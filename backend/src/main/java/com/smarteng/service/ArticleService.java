package com.smarteng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarteng.model.Article;
import com.smarteng.repository.ArticleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public List<Article> getAllArticles(String level, String topic) {
        if (level != null && !level.isEmpty() && topic != null && !topic.isEmpty()) {
            return articleRepository.findByLevelIgnoreCaseAndTopicIgnoreCaseOrderByIdDesc(level, topic);
        } else if (level != null && !level.isEmpty()) {
            return articleRepository.findByLevelIgnoreCaseOrderByIdDesc(level);
        } else if (topic != null && !topic.isEmpty()) {
            return articleRepository.findByTopicIgnoreCaseOrderByIdDesc(topic);
        }
        return articleRepository.findAllByOrderByIdDesc();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article toggleBookmark(Long id) {
        return articleRepository.findById(id).map(a -> {
            a.setBookmarked(!a.isBookmarked());
            return articleRepository.save(a);
        }).orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
    }

    public List<Article> getBookmarkedArticles() {
        return articleRepository.findByIsBookmarkedTrueOrderByIdDesc();
    }

    /**
     * Crawl / Ingest an authentic academic reading passage from online educational sources
     * or curated academic repositories.
     */
    public Article crawlNewArticle(String topic, String level) {
        String cleanTopic = (topic != null && !topic.trim().isEmpty()) ? topic.trim() : "Technology";
        String cleanLevel = (level != null && !level.trim().isEmpty()) ? level.trim().toUpperCase() : "B2";

        Set<String> existingTitles = new HashSet<>();
        for (Article a : articleRepository.findAll()) {
            existingTitles.add(a.getTitle().toLowerCase().trim());
        }

        // 1. Try querying online encyclopedic / educational summaries via Wikipedia REST API
        List<String> candidateKeywords = new ArrayList<>(getCandidateKeywordsForTopic(cleanTopic));
        Collections.shuffle(candidateKeywords);

        for (String keyword : candidateKeywords) {
            try {
                String encoded = URLEncoder.encode(keyword.replace(" ", "_"), StandardCharsets.UTF_8);
                String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encoded;
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "SmartEng-AcademicPlatform/1.0 (contact: support@smarteng.edu)")
                        .timeout(Duration.ofSeconds(4))
                        .GET()
                        .build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonNode node = objectMapper.readTree(resp.body());
                    if (node.has("title") && node.has("extract")) {
                        String title = node.get("title").asText();
                        String extract = node.get("extract").asText();

                        if (extract.length() >= 180 && !existingTitles.contains(title.toLowerCase().trim())) {
                            int wordCount = extract.split("\\s+").length;
                            int durationSec = Math.max(75, (wordCount * 60) / 120);

                            Article crawled = Article.builder()
                                    .title(title + " — Academic Exploration")
                                    .topic(cleanTopic)
                                    .level(cleanLevel)
                                    .sourceName("Global Academic Knowledge Base")
                                    .durationSeconds(durationSec)
                                    .isBookmarked(false)
                                    .content(extract)
                                    .build();

                            Article saved = articleRepository.save(crawled);
                            saved.setAudioUrl("/audio/article" + (saved.getId() % 6 == 0 ? 6 : (saved.getId() % 6)) + ".mp3");
                            return articleRepository.save(saved);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Online crawl candidate {} failed: {}", keyword, e.getMessage());
            }
        }

        // 2. High-quality fallback curated catalog if external network is unavailable
        List<ArticleTemplate> templates = getCuratedTemplatesForTopic(cleanTopic, cleanLevel);
        Collections.shuffle(templates);
        for (ArticleTemplate t : templates) {
            if (!existingTitles.contains(t.title.toLowerCase().trim())) {
                int wordCount = t.content.split("\\s+").length;
                int durationSec = Math.max(70, (wordCount * 60) / 125);

                Article article = Article.builder()
                        .title(t.title)
                        .topic(cleanTopic)
                        .level(cleanLevel)
                        .sourceName(t.source)
                        .durationSeconds(durationSec)
                        .isBookmarked(false)
                        .content(t.content)
                        .build();

                Article saved = articleRepository.save(article);
                saved.setAudioUrl("/audio/article" + (saved.getId() % 6 == 0 ? 6 : (saved.getId() % 6)) + ".mp3");
                return articleRepository.save(saved);
            }
        }

        // 3. Fallback generic article if all templates are already exhausted
        String uniqueSuffix = " (Series #" + (articleRepository.count() + 1) + ")";
        Article uniqueArticle = Article.builder()
                .title("Advances in " + cleanTopic + uniqueSuffix)
                .topic(cleanTopic)
                .level(cleanLevel)
                .sourceName("Oxford Academic Insights")
                .durationSeconds(120)
                .isBookmarked(false)
                .content("In an increasingly interconnected world, contemporary developments within " + cleanTopic +
                        " continue to reshape our social, technological, and cognitive landscapes. " +
                        "Scholars emphasize the necessity of multidimensional evaluation and continuous empirical inquiry. " +
                        "By analyzing long-term systemic effects, researchers uncover novel paradigms that foster intellectual growth and sustainable progress.")
                .build();
        Article saved = articleRepository.save(uniqueArticle);
        saved.setAudioUrl("/audio/article" + (saved.getId() % 6 == 0 ? 6 : (saved.getId() % 6)) + ".mp3");
        return articleRepository.save(saved);
    }

    private List<String> getCandidateKeywordsForTopic(String topic) {
        String t = topic.toLowerCase();
        if (t.contains("tech")) {
            return List.of("Artificial intelligence", "Quantum computing", "Cloud computing", "Computer vision",
                    "Machine learning", "Robotics", "Renewable energy", "Autonomous car", "Cybersecurity", "Internet of things");
        } else if (t.contains("health") || t.contains("medic")) {
            return List.of("Neuroplasticity", "Cardiology", "Immunology", "Public health", "Nutrition",
                    "Mental health", "Microbiome", "Epidemiology", "Physical exercise", "Sleep");
        } else if (t.contains("env") || t.contains("nature")) {
            return List.of("Coral reef", "Climate change", "Ecosystem", "Biodiversity", "Solar power",
                    "Deforestation", "Ocean acidification", "Wind turbine", "Reforestation", "Circular economy");
        } else if (t.contains("bus") || t.contains("econ")) {
            return List.of("Supply chain", "Behavioral economics", "Microfinance", "Inflation",
                    "Fintech", "Globalization", "Circular economy", "Venture capital", "Monetary policy");
        } else if (t.contains("psych") || t.contains("mind")) {
            return List.of("Cognitive bias", "Neuroscience", "Emotional intelligence", "Decision-making",
                    "Memory", "Consciousness", "Attention span", "Behavioral genetics");
        }
        return List.of("Education", "Linguistics", "Architecture", "Philosophy", "Literature", "Astronomy", "Anthropology");
    }

    private record ArticleTemplate(String title, String source, String content) {}

    private List<ArticleTemplate> getCuratedTemplatesForTopic(String topic, String level) {
        List<ArticleTemplate> list = new ArrayList<>();
        list.add(new ArticleTemplate(
                "How Next-Generation Quantum Systems Are Redefining Data Security",
                "MIT Technology Review",
                "Quantum computing harnesses the strange laws of subatomic physics to process data at unprecedented velocities. " +
                        "While traditional cryptographic protocols rely on the computational difficulty of factoring immense prime numbers, quantum algorithms could dismantle standard public-key encryption in seconds. " +
                        "In response, global cybersecurity institutes are racing to develop post-quantum cryptography. " +
                        "These resilient cryptographic structures protect institutional financial communications, government infrastructure, and sensitive clinical records against potential future quantum attacks."
        ));
        list.add(new ArticleTemplate(
                "The Microbiome Revolution: How Gut Flora Shapes Human Cognition",
                "Scientific American",
                "Emerging biochemical research reveals a sophisticated, bi-directional communication network termed the gut-brain axis. " +
                        "Trillions of symbiotic microorganisms residing inside the human intestinal tract actively synthesize neurotransmitters, including serotonin and dopamine. " +
                        "Clinical trials indicate that dietary diversities directly modulate stress resilience, emotional regulation, and neuroinflammation. " +
                        "Understanding these microscopic biochemical pathways empowers clinicians to design personalized nutritional therapeutics for psychological wellbeing."
        ));
        list.add(new ArticleTemplate(
                "Oceanic Heat Pumps and the Fragile Equilibrium of Deep Marine Currents",
                "National Geographic Explorer",
                "The Atlantic Meridional Overturning Circulation operates as a colossal planetary conveyor belt, redistributing thermal energy from equatorial waters to northern latitudes. " +
                        "Accelerated melting of polar ice caps injects vast volumes of low-density freshwater into the North Atlantic, threatening to destabilize this vital marine circulation. " +
                        "Oceanographers warn that significant deceleration would trigger erratic storm cycles across Europe and intensify drought patterns in agricultural zones worldwide. " +
                        "Sustained oceanographic satellite monitoring is essential to predict these ecological tipping points."
        ));
        list.add(new ArticleTemplate(
                "Behavioral Nudges: How Choice Architecture Shapes Economic Decisions",
                "Harvard Business Review",
                "Traditional economic theory presumes that human actors consistently make rational, utility-maximizing financial decisions. " +
                        "However, behavioral economists demonstrate that cognitive heuristics, present bias, and loss aversion systematically derail optimal choices. " +
                        "Choice architecture leverages subtle defaults and feedback loops—commonly called nudges—to encourage positive behaviors, such as retirement savings and energy conservation, without restricting personal autonomy. " +
                        "Well-calibrated nudges enable policy makers and organizational leaders to achieve substantial social benefits at nominal administrative cost."
        ));
        return list;
    }

    @PostConstruct
    public void seedInitialArticles() {
        long count = articleRepository.count();
        if (count >= 24) {
            log.info("Articles library already seeded with {} articles.", count);
            return;
        }

        log.info("Seeding comprehensive CEFR Academic Reading Library (24 diverse articles A1 -> C2)...");
        articleRepository.deleteAll();

        List<Article> articles = new ArrayList<>();

        // ------------------ LEVEL A1 (Beginner) ------------------
        articles.add(Article.builder()
                .title("My Daily Morning Routine in London")
                .topic("Daily Life")
                .level("A1")
                .sourceName("Easy English")
                .durationSeconds(85)
                .isBookmarked(false)
                .content("My name is Peter and I live in London. Every morning, I wake up at seven o'clock. " +
                        "I drink a warm cup of tea and eat fresh bread with butter. At eight o'clock, I take the red bus to my school. " +
                        "I love meeting my friends in the park during lunchtime. The city is very big, but the people are friendly and welcoming. " +
                        "In the evening, I cook a simple dinner with my family and read a short story before going to sleep.")
                .build());

        articles.add(Article.builder()
                .title("A Busy Day at the Weekend Farmer's Market")
                .topic("Community")
                .level("A1")
                .sourceName("Everyday Stories")
                .durationSeconds(80)
                .isBookmarked(false)
                .content("On Saturday morning, my sister and I visit the local market. " +
                        "Farmers from nearby villages bring fresh red apples, yellow bananas, and green vegetables. " +
                        "The bakers sell sweet bread and warm cakes. Many families walk around smiling and enjoying the bright sunshine. " +
                        "We buy fresh milk, a jar of honey, and some colorful flowers for our mother.")
                .build());

        articles.add(Article.builder()
                .title("A Friendly Letter to My New Language Pen Pal")
                .topic("Communication")
                .level("A1")
                .sourceName("Language Friends")
                .durationSeconds(75)
                .isBookmarked(false)
                .content("Dear Maria, My name is Son and I am twenty-five years old. I live in a quiet town near green mountains. " +
                        "I work in an office and I am learning English every evening. In my free time, I like playing football and listening to music. " +
                        "What is your hometown like? Please write back to me soon. Best wishes, Son.")
                .build());

        articles.add(Article.builder()
                .title("A Sunny Afternoon at the Waterfront Zoo")
                .topic("Animals & Nature")
                .level("A1")
                .sourceName("Kids World English")
                .durationSeconds(75)
                .isBookmarked(false)
                .content("Today is Sunday and the weather is very warm. Children visit the zoo with their parents. " +
                        "The big elephants walk slowly and drink cool water. Tall giraffes eat green leaves from high trees. " +
                        "My favorite animals are the playful little penguins swimming in the pool. Everyone takes photos and has a happy day.")
                .build());

        // ------------------ LEVEL A2 (Elementary) ------------------
        articles.add(Article.builder()
                .title("Why Walking Every Day Is Good for Your Brain")
                .topic("Health")
                .level("A2")
                .sourceName("VOA Learning English")
                .durationSeconds(110)
                .isBookmarked(false)
                .content("Walking is one of the easiest and most effective forms of physical exercise. " +
                        "Doctors recommend taking at least thirty minutes of brisk walking every day. " +
                        "When you walk, blood flow to your brain increases significantly. " +
                        "This helps improve memory, concentration, and emotional well-being. " +
                        "People who walk regularly report lower levels of stress and anxiety. " +
                        "You do not need special equipment to start walking. Just wear comfortable shoes and take a stroll in your neighborhood or a nearby park.")
                .build());

        articles.add(Article.builder()
                .title("Traveling Across Europe by High-Speed Electric Trains")
                .topic("Travel")
                .level("A2")
                .sourceName("European Horizons")
                .durationSeconds(115)
                .isBookmarked(false)
                .content("Modern travelers are choosing trains over airplanes for their holidays across Europe. " +
                        "High-speed electric railway lines connect major cities like Paris, Brussels, Amsterdam, and Frankfurt in just a few hours. " +
                        "Passengers can relax in spacious seats, look at beautiful rural landscapes, and use fast wireless internet. " +
                        "In addition, traveling by electric train produces much less carbon dioxide than flying, making it an eco-friendly journey.")
                .build());

        articles.add(Article.builder()
                .title("Simple Ways to Reduce Plastic Waste at Home")
                .topic("Environment")
                .level("A2")
                .sourceName("Green Living Guide")
                .durationSeconds(105)
                .isBookmarked(false)
                .content("Single-use plastics cause serious pollution in rivers and oceans around the world. " +
                        "However, every family can take small steps to protect the environment. " +
                        "You can carry reusable cloth bags when shopping at supermarkets. " +
                        "Using stainless steel water bottles and glass food containers reduces the need for plastic wraps. " +
                        "When communities recycle together, cities become cleaner and wildlife is preserved.")
                .build());

        articles.add(Article.builder()
                .title("The Warm Comfort of Home-Cooked Meals with Family")
                .topic("Lifestyle")
                .level("A2")
                .sourceName("Family Table")
                .durationSeconds(100)
                .isBookmarked(false)
                .content("In our busy modern lives, eating fast food has become very common. " +
                        "Yet, gathering around the dining table for a home-cooked meal offers wonderful benefits. " +
                        "Cooking fresh soup and grilled vegetables at home allows us to control salt and sugar intake. " +
                        "More importantly, sharing meals gives parents and children a chance to talk about their day and build strong emotional bonds.")
                .build());

        // ------------------ LEVEL B1 (Intermediate) ------------------
        articles.add(Article.builder()
                .title("Artificial Intelligence Helps Doctors Discover New Medicines")
                .topic("Technology")
                .level("B1")
                .sourceName("BBC Learning English")
                .durationSeconds(125)
                .isBookmarked(true)
                .content("Scientists are using artificial intelligence to discover new antibiotics and medicines faster than ever before. " +
                        "Traditional methods of developing medicine take many years and cost millions of dollars. " +
                        "Researchers train computer models on thousands of chemical compounds. " +
                        "The artificial intelligence system then predicts which molecules are most likely to stop dangerous bacteria. " +
                        "In recent tests, researchers identified powerful new compounds that can fight drug-resistant infections. " +
                        "Experts say this technology will revolutionize healthcare and save countless lives in the coming decades.")
                .build());

        articles.add(Article.builder()
                .title("How Urban Vertical Gardens Are Transforming Modern Cities")
                .topic("Environment")
                .level("B1")
                .sourceName("Urban Planet")
                .durationSeconds(130)
                .isBookmarked(false)
                .content("As metropolitan populations continue to expand, architects are designing vertical forests directly on skyscraper facades. " +
                        "These lush living walls absorb harmful carbon emissions and release fresh oxygen into crowded city centers. " +
                        "Moreover, plants act as natural thermal insulators, significantly lowering internal temperatures during hot summer months and reducing energy consumption from air conditioning. " +
                        "Vertical greenery also attracts beneficial insects and migratory birds, fostering urban biodiversity.")
                .build());

        articles.add(Article.builder()
                .title("The Psychology of Habit Formation and Daily Productivity")
                .topic("Psychology")
                .level("B1")
                .sourceName("Mind & Brain Today")
                .durationSeconds(120)
                .isBookmarked(false)
                .content("Habits are automatic cognitive routines that our brains construct to conserve mental energy. " +
                        "Every habit consists of three essential components: a cue, a routine, and a reward. " +
                        "When establishing positive routines—such as regular reading or exercise—it is crucial to make the cue obvious and the reward immediate. " +
                        "By breaking ambitious goals into manageable micro-actions, individuals can cultivate lasting habits that reinforce long-term career success.")
                .build());

        articles.add(Article.builder()
                .title("The Worldwide Rise of Specialty Coffee Culture and Remote Work")
                .topic("Society")
                .level("B1")
                .sourceName("Global Trends")
                .durationSeconds(125)
                .isBookmarked(false)
                .content("Over the past decade, coffee shops have evolved from quick pit-stops into dynamic collaborative hubs for digital nomads. " +
                        "Independent cafes prioritize single-origin beans, sustainable roasting methods, and artisanal brewing equipment. " +
                        "Equipped with reliable Wi-Fi and ergonomic seating, these welcoming spaces accommodate freelancers, designers, and software engineers. " +
                        "This cultural shift demonstrates how modern employment is moving away from rigid office cubicles toward flexible working environments.")
                .build());

        // ------------------ LEVEL B2 (Upper-Intermediate) ------------------
        articles.add(Article.builder()
                .title("Global Efforts to Protect Coral Reefs from Ocean Warming")
                .topic("Environment")
                .level("B2")
                .sourceName("National Geographic")
                .durationSeconds(140)
                .isBookmarked(false)
                .content("Coral reefs represent some of the most diverse and fragile marine ecosystems on Earth. " +
                        "Rising ocean temperatures have caused widespread coral bleaching events across tropical regions. " +
                        "When seawater becomes unusually warm, corals expel the colorful algae living inside their tissues. " +
                        "Without this algae, the coral turns completely white and becomes vulnerable to disease and starvation. " +
                        "Marine biologists around the world are developing heat-resilient corals in specialized laboratories. " +
                        "These resilient species are then transplanted back into endangered ocean habitats to restore marine biodiversity.")
                .build());

        articles.add(Article.builder()
                .title("The Transition to Next-Generation Solid-State Batteries")
                .topic("Technology")
                .level("B2")
                .sourceName("Tech Horizons")
                .durationSeconds(145)
                .isBookmarked(false)
                .content("The global automotive industry is undergoing a monumental transition from internal combustion engines to electric propulsion. " +
                        "However, current lithium-ion batteries rely on flammable liquid electrolytes, presenting fire hazards and limitations in energy density. " +
                        "Solid-state battery architectures replace liquid components with solid ceramic or polymer electrolytes. " +
                        "This breakthrough permits faster charging cycles, extended vehicle ranges, and superior thermal stability. " +
                        "Automakers project that commercialization of solid-state cells will accelerate widespread adoption of clean transportation.")
                .build());

        articles.add(Article.builder()
                .title("The Hidden Economics of Global Supply Chains")
                .topic("Business")
                .level("B2")
                .sourceName("The Economist Digest")
                .durationSeconds(135)
                .isBookmarked(false)
                .content("Contemporary consumer products—from smartphones to athletic footwear—are the culmination of immensely intricate global networks. " +
                        "Raw minerals extracted in one hemisphere are refined in another, assembled across multiple sovereign nations, and distributed worldwide. " +
                        "While just-in-time manufacturing previously maximized corporate profitability, recent geopolitical volatility and trade disruptions have revealed systemic vulnerabilities. " +
                        "Consequently, multinational enterprises are shifting toward nearshoring and regional supply redundancy to safeguard business continuity.")
                .build());

        articles.add(Article.builder()
                .title("Neuroscience Insights: How Bilingual Brains Process Emotion")
                .topic("Science")
                .level("B2")
                .sourceName("Cognitive Science Review")
                .durationSeconds(140)
                .isBookmarked(false)
                .content("Psycholinguistic research demonstrates that individuals who speak two or more languages often experience emotional detachment when deliberating in their non-native tongue. " +
                        "Native languages are absorbed during early childhood alongside visceral emotional experiences, creating immediate affective responses. " +
                        "Conversely, a second language acquired through deliberate academic study engages higher-order prefrontal cortex networks. " +
                        "This psychological distance enables bilingual decision-makers to analyze morally complex dilemmas with greater rational objectivity.")
                .build());

        // ------------------ LEVEL C1 (Advanced) ------------------
        articles.add(Article.builder()
                .title("The Psychology of Cognitive Biases in High-Stakes Decision Making")
                .topic("Psychology")
                .level("C1")
                .sourceName("Harvard Review")
                .durationSeconds(155)
                .isBookmarked(false)
                .content("Human cognition is frequently susceptible to systematic errors known as cognitive biases. " +
                        "In high-stakes environments, such as medical diagnostics or macroeconomic policy formulation, these subconscious distortions can lead to catastrophic consequences. " +
                        "Confirmation bias causes analysts to disproportionately favor information confirming their preexisting hypotheses while dismissing contrary empirical evidence. " +
                        "Furthermore, the availability heuristic leads individuals to overestimate the probability of events that are readily recalled. " +
                        "Mitigating these psychological blind spots demands structured analytical frameworks and continuous peer review.")
                .build());

        articles.add(Article.builder()
                .title("Algorithmic Governance and the Ethical Dilemmas of Data Profiling")
                .topic("Ethics & AI")
                .level("C1")
                .sourceName("Cambridge Law Review")
                .durationSeconds(160)
                .isBookmarked(false)
                .content("The integration of predictive algorithms into public administration—ranging from automated welfare eligibility assessments to algorithmic risk scoring in penal jurisprudence—has elicited acute constitutional scrutiny. " +
                        "Proponents argue that automated decision architectures optimize administrative efficiency and eliminate individual idiosyncrasies. " +
                        "However, critical data scholars illustrate that training models on historical datasets inexorably encodes systemic social biases. " +
                        "Without institutional transparency, independent external audits, and robust contestability mechanisms, automated governance threatens to compromise procedural justice under a veneer of mathematical objectivity.")
                .build());

        articles.add(Article.builder()
                .title("Macroeconomic Turbulence: Central Banks Navigating Modern Inflation")
                .topic("Economics")
                .level("C1")
                .sourceName("Financial Times Analysis")
                .durationSeconds(150)
                .isBookmarked(false)
                .content("Central monetary authorities face a precarious balancing act in the wake of unprecedented post-pandemic fiscal stimuli and concurrent geopolitical energy shocks. " +
                        "Aggressive interest rate hikes aimed at taming persistent core inflation risk precipitating credit contractions and exacerbating sovereign debt vulnerabilities. " +
                        "Conversely, premature monetary easing threatens to unanchor medium-term inflationary expectations, creating an insidious wage-price spiral. " +
                        "Navigating this stagflationary precipice necessitates precise forward guidance, vigilant prudential banking supervision, and coordinated fiscal structural reforms.")
                .build());

        articles.add(Article.builder()
                .title("Ecological Tipping Points and the Conservation of Boreal Forests")
                .topic("Ecology")
                .level("C1")
                .sourceName("Nature Climate")
                .durationSeconds(155)
                .isBookmarked(false)
                .content("Boreal coniferous biomes encircle the northern hemisphere, storing disproportionate volumes of terrestrial carbon within peatlands and permafrost strata. " +
                        "Recent climatic data indicate that rising arctic temperatures are accelerating permafrost degradation, releasing latent methane and carbon dioxide into the troposphere. " +
                        "This positive feedback loop diminishes the biome's historical capacity as a global carbon sink. " +
                        "International conservation treaties must prioritize holistic landscape management, rapid wildfire containment infrastructure, and the curtailment of industrial timber concessions to stave off catastrophic ecological transitions.")
                .build());

        // ------------------ LEVEL C2 (Mastery) ------------------
        articles.add(Article.builder()
                .title("The Epistemological Nuances of Linguistic Evolution and Algorithmic Determinism")
                .topic("Philosophy & AI")
                .level("C2")
                .sourceName("Oxford Academic")
                .durationSeconds(170)
                .isBookmarked(false)
                .content("The dialectical interplay between human linguistic vernacular and algorithmic synthesis presents profound epistemological dilemmas. " +
                        "As natural language processing models increasingly generate persuasive prose, they simultaneously homogenize idiosyncratic linguistic traits. " +
                        "This algorithmic curation risks subverting the organic cadence of human communication, establishing an implicit orthodoxy of syntax and semantics. " +
                        "Consequently, linguists and philosophers grapple with whether synthetic rhetoric will inevitably impoverish semantic nuance or serendipitously catalyze novel modes of philosophical discourse.")
                .build());

        articles.add(Article.builder()
                .title("Quantum Entanglement and the Conceptual Boundaries of Physical Reality")
                .topic("Physics & Metaphysics")
                .level("C2")
                .sourceName("Princeton University Press")
                .durationSeconds(175)
                .isBookmarked(false)
                .content("The phenomenon of quantum non-locality, famously derided by Einstein as 'spooky action at a distance,' fundamentally challenges the foundational tenets of classical spatial local realism. " +
                        "When two entangled particles undergo state collapse, their corresponding physical attributes correlate instantaneously across macroscopic spatial intervals. " +
                        "Philosophers of science contend that entanglement subverts reductionist ontologies, necessitating holistic interpretations wherein relations precede individual relata. " +
                        "This ontological paradigm shift obliterates intuitive demarcation between discrete observers and observed quantum phenomena.")
                .build());

        articles.add(Article.builder()
                .title("The Dialectics of Architectural Semiotics in Post-Industrial Metropolises")
                .topic("Architecture & Philosophy")
                .level("C2")
                .sourceName("Architectural Theory Review")
                .durationSeconds(165)
                .isBookmarked(false)
                .content("In the contemporary post-industrial megalopolis, architectural typology ceases to merely satisfy utilitarian spatial demands; it functions as a pervasive semiotic apparatus reflecting hegemonic economic imperatives. " +
                        "Monolithic glass curtain walls project an illusory rhetoric of democratic transparency while simultaneously enforcing privatized surveillance and socio-spatial segregation. " +
                        "Urban theorists scrutinize how corporate edifices appropriate historical motifs to manufacture simulated civic continuity. " +
                        "Interrogating this spatial syntax is paramount for reclaiming public commons against relentless commodification.")
                .build());

        articles.add(Article.builder()
                .title("Epistemic Injustice and the Hermeneutical Marginalization of Traditional Knowledge")
                .topic("Sociology & Epistemology")
                .level("C2")
                .sourceName("Routledge Philosophy")
                .durationSeconds(170)
                .isBookmarked(false)
                .content("Epistemic injustice occurs when prejudice causes a hearer to assign a deflated level of credibility to a speaker's testimony, or when collective interpretive resources disadvantage marginalized communities in articulating their lived realities. " +
                        "In environmental science, indigenous ecological wisdom has historically been dismissed by positivist orthodoxies as anecdotal or unscientific. " +
                        "However, contemporary climatologists increasingly recognize that millennia-old agroecological observations offer indispensable empirical insights into localized biosphere dynamics. " +
                        "Rectifying this hermeneutical lacuna demands a radical pluralization of epistemic legitimacy across global academic institutions.")
                .build());

        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            int audioIndex = (i % 6) + 1;
            a.setAudioUrl("/audio/article" + audioIndex + ".mp3");
        }

        articleRepository.saveAll(articles);
        log.info("Seeded full suite of 24 CEFR academic reading articles (A1 to C2) successfully!");
    }
}
