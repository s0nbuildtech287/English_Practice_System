import asyncio
import os
import edge_tts

ARTICLES = [
    # A1 (1-4)
    (
        1,
        "My name is Peter and I live in London. Every morning, I wake up at seven o'clock. "
        "I drink a warm cup of tea and eat fresh bread with butter. At eight o'clock, I take the red bus to my school. "
        "I love meeting my friends in the park during lunchtime. The city is very big, but the people are friendly and welcoming. "
        "In the evening, I cook a simple dinner with my family and read a short story before going to sleep."
    ),
    (
        2,
        "On Saturday morning, my sister and I visit the local market. "
        "Farmers from nearby villages bring fresh red apples, yellow bananas, and green vegetables. "
        "The bakers sell sweet bread and warm cakes. Many families walk around smiling and enjoying the bright sunshine. "
        "We buy fresh milk, a jar of honey, and some colorful flowers for our mother."
    ),
    (
        3,
        "Dear Maria, My name is Son and I am twenty-five years old. I live in a quiet town near green mountains. "
        "I work in an office and I am learning English every evening. In my free time, I like playing football and listening to music. "
        "What is your hometown like? Please write back to me soon. Best wishes, Son."
    ),
    (
        4,
        "Today is Sunday and the weather is very warm. Children visit the zoo with their parents. "
        "The big elephants walk slowly and drink cool water. Tall giraffes eat green leaves from high trees. "
        "My favorite animals are the playful little penguins swimming in the pool. Everyone takes photos and has a happy day."
    ),

    # A2 (5-8)
    (
        5,
        "Walking is one of the easiest and most effective forms of physical exercise. "
        "Doctors recommend taking at least thirty minutes of brisk walking every day. "
        "When you walk, blood flow to your brain increases significantly. "
        "This helps improve memory, concentration, and emotional well-being. "
        "People who walk regularly report lower levels of stress and anxiety. "
        "You do not need special equipment to start walking. Just wear comfortable shoes and take a stroll in your neighborhood or a nearby park."
    ),
    (
        6,
        "Modern travelers are choosing trains over airplanes for their holidays across Europe. "
        "High-speed electric railway lines connect major cities like Paris, Brussels, Amsterdam, and Frankfurt in just a few hours. "
        "Passengers can relax in spacious seats, look at beautiful rural landscapes, and use fast wireless internet. "
        "In addition, traveling by electric train produces much less carbon dioxide than flying, making it an eco-friendly journey."
    ),
    (
        7,
        "Single-use plastics cause serious pollution in rivers and oceans around the world. "
        "However, every family can take small steps to protect the environment. "
        "You can carry reusable cloth bags when shopping at supermarkets. "
        "Using stainless steel water bottles and glass food containers reduces the need for plastic wraps. "
        "When communities recycle together, cities become cleaner and wildlife is preserved."
    ),
    (
        8,
        "In our busy modern lives, eating fast food has become very common. "
        "Yet, gathering around the dining table for a home-cooked meal offers wonderful benefits. "
        "Cooking fresh soup and grilled vegetables at home allows us to control salt and sugar intake. "
        "More importantly, sharing meals gives parents and children a chance to talk about their day and build strong emotional bonds."
    ),

    # B1 (9-12)
    (
        9,
        "Scientists are using artificial intelligence to discover new antibiotics and medicines faster than ever before. "
        "Traditional methods of developing medicine take many years and cost millions of dollars. "
        "Researchers train computer models on thousands of chemical compounds. "
        "The artificial intelligence system then predicts which molecules are most likely to stop dangerous bacteria. "
        "In recent tests, researchers identified powerful new compounds that can fight drug-resistant infections. "
        "Experts say this technology will revolutionize healthcare and save countless lives in the coming decades."
    ),
    (
        10,
        "As metropolitan populations continue to expand, architects are designing vertical forests directly on skyscraper facades. "
        "These lush living walls absorb harmful carbon emissions and release fresh oxygen into crowded city centers. "
        "Moreover, plants act as natural thermal insulators, significantly lowering internal temperatures during hot summer months and reducing energy consumption from air conditioning. "
        "Vertical greenery also attracts beneficial insects and migratory birds, fostering urban biodiversity."
    ),
    (
        11,
        "Habits are automatic cognitive routines that our brains construct to conserve mental energy. "
        "Every habit consists of three essential components: a cue, a routine, and a reward. "
        "When establishing positive routines—such as regular reading or exercise—it is crucial to make the cue obvious and the reward immediate. "
        "By breaking ambitious goals into manageable micro-actions, individuals can cultivate lasting habits that reinforce long-term career success."
    ),
    (
        12,
        "Over the past decade, coffee shops have evolved from quick pit-stops into dynamic collaborative hubs for digital nomads. "
        "Independent cafes prioritize single-origin beans, sustainable roasting methods, and artisanal brewing equipment. "
        "Equipped with reliable Wi-Fi and ergonomic seating, these welcoming spaces accommodate freelancers, designers, and software engineers. "
        "This cultural shift demonstrates how modern employment is moving away from rigid office cubicles toward flexible working environments."
    ),

    # B2 (13-16)
    (
        13,
        "Coral reefs represent some of the most diverse and fragile marine ecosystems on Earth. "
        "Rising ocean temperatures have caused widespread coral bleaching events across tropical regions. "
        "When seawater becomes unusually warm, corals expel the colorful algae living inside their tissues. "
        "Without this algae, the coral turns completely white and becomes vulnerable to disease and starvation. "
        "Marine biologists around the world are developing heat-resilient corals in specialized laboratories. "
        "These resilient species are then transplanted back into endangered ocean habitats to restore marine biodiversity."
    ),
    (
        14,
        "The global automotive industry is undergoing a monumental transition from internal combustion engines to electric propulsion. "
        "However, current lithium-ion batteries rely on flammable liquid electrolytes, presenting fire hazards and limitations in energy density. "
        "Solid-state battery architectures replace liquid components with solid ceramic or polymer electrolytes. "
        "This breakthrough permits faster charging cycles, extended vehicle ranges, and superior thermal stability. "
        "Automakers project that commercialization of solid-state cells will accelerate widespread adoption of clean transportation."
    ),
    (
        15,
        "Contemporary consumer products—from smartphones to athletic footwear—are the culmination of immensely intricate global networks. "
        "Raw minerals extracted in one hemisphere are refined in another, assembled across multiple sovereign nations, and distributed worldwide. "
        "While just-in-time manufacturing previously maximized corporate profitability, recent geopolitical volatility and trade disruptions have revealed systemic vulnerabilities. "
        "Consequently, multinational enterprises are shifting toward nearshoring and regional supply redundancy to safeguard business continuity."
    ),
    (
        16,
        "Psycholinguistic research demonstrates that individuals who speak two or more languages often experience emotional detachment when deliberating in their non-native tongue. "
        "Native languages are absorbed during early childhood alongside visceral emotional experiences, creating immediate affective responses. "
        "Conversely, a second language acquired through deliberate academic study engages higher-order prefrontal cortex networks. "
        "This psychological distance enables bilingual decision-makers to analyze morally complex dilemmas with greater rational objectivity."
    ),

    # C1 (17-20)
    (
        17,
        "Human cognition is frequently susceptible to systematic errors known as cognitive biases. "
        "In high-stakes environments, such as medical diagnostics or macroeconomic policy formulation, these subconscious distortions can lead to catastrophic consequences. "
        "Confirmation bias causes analysts to disproportionately favor information confirming their preexisting hypotheses while dismissing contrary empirical evidence. "
        "Furthermore, the availability heuristic leads individuals to overestimate the probability of events that are readily recalled. "
        "Mitigating these psychological blind spots demands structured analytical frameworks and continuous peer review."
    ),
    (
        18,
        "The integration of predictive algorithms into public administration—ranging from automated welfare eligibility assessments to algorithmic risk scoring in penal jurisprudence—has elicited acute constitutional scrutiny. "
        "Proponents argue that automated decision architectures optimize administrative efficiency and eliminate individual idiosyncrasies. "
        "However, critical data scholars illustrate that training models on historical datasets inexorably encodes systemic social biases. "
        "Without institutional transparency, independent external audits, and robust contestability mechanisms, automated governance threatens to compromise procedural justice under a veneer of mathematical objectivity."
    ),
    (
        19,
        "Central monetary authorities face a precarious balancing act in the wake of unprecedented post-pandemic fiscal stimuli and concurrent geopolitical energy shocks. "
        "Aggressive interest rate hikes aimed at taming persistent core inflation risk precipitating credit contractions and exacerbating sovereign debt vulnerabilities. "
        "Conversely, premature monetary easing threatens to unanchor medium-term inflationary expectations, creating an insidious wage-price spiral. "
        "Navigating this stagflationary precipice necessitates precise forward guidance, vigilant prudential banking supervision, and coordinated fiscal structural reforms."
    ),
    (
        20,
        "Boreal coniferous biomes encircle the northern hemisphere, storing disproportionate volumes of terrestrial carbon within peatlands and permafrost strata. "
        "Recent climatic data indicate that rising arctic temperatures are accelerating permafrost degradation, releasing latent methane and carbon dioxide into the troposphere. "
        "This positive feedback loop diminishes the biome's historical capacity as a global carbon sink. "
        "International conservation treaties must prioritize holistic landscape management, rapid wildfire containment infrastructure, and the curtailment of industrial timber concessions to stave off catastrophic ecological transitions."
    ),

    # C2 (21-24)
    (
        21,
        "The dialectical interplay between human linguistic vernacular and algorithmic synthesis presents profound epistemological dilemmas. "
        "As natural language processing models increasingly generate persuasive prose, they simultaneously homogenize idiosyncratic linguistic traits. "
        "This algorithmic curation risks subverting the organic cadence of human communication, establishing an implicit orthodoxy of syntax and semantics. "
        "Consequently, linguists and philosophers grapple with whether synthetic rhetoric will inevitably impoverish semantic nuance or serendipitously catalyze novel modes of philosophical discourse."
    ),
    (
        22,
        "The phenomenon of quantum non-locality, famously derided by Einstein as 'spooky action at a distance,' fundamentally challenges the foundational tenets of classical spatial local realism. "
        "When two entangled particles undergo state collapse, their corresponding physical attributes correlate instantaneously across macroscopic spatial intervals. "
        "Philosophers of science contend that entanglement subverts reductionist ontologies, necessitating holistic interpretations wherein relations precede individual relata. "
        "This ontological paradigm shift obliterates intuitive demarcation between discrete observers and observed quantum phenomena."
    ),
    (
        23,
        "In the contemporary post-industrial megalopolis, architectural typology ceases to merely satisfy utilitarian spatial demands; it functions as a pervasive semiotic apparatus reflecting hegemonic economic imperatives. "
        "Monolithic glass curtain walls project an illusory rhetoric of democratic transparency while simultaneously enforcing privatized surveillance and socio-spatial segregation. "
        "Urban theorists scrutinize how corporate edifices appropriate historical motifs to manufacture simulated civic continuity. "
        "Interrogating this spatial syntax is paramount for reclaiming public commons against relentless commodification."
    ),
    (
        24,
        "Epistemic injustice occurs when prejudice causes a hearer to assign a deflated level of credibility to a speaker's testimony, or when collective interpretive resources disadvantage marginalized communities in articulating their lived realities. "
        "In environmental science, indigenous ecological wisdom has historically been dismissed by positivist orthodoxies as anecdotal or unscientific. "
        "However, contemporary climatologists increasingly recognize that millennia-old agroecological observations offer indispensable empirical insights into localized biosphere dynamics. "
        "Rectifying this hermeneutical lacuna demands a radical pluralization of epistemic legitimacy across global academic institutions."
    ),
]

VOICE = "en-US-ChristopherNeural"

OUTPUT_DIRS = [
    r"backend\src\main\resources\static\audio",
    r"backend\build\resources\main\static\audio",
]

async def generate():
    for d in OUTPUT_DIRS:
        os.makedirs(d, exist_ok=True)

    print(f"Starting neural audio generation for {len(ARTICLES)} articles...")
    for idx, text in ARTICLES:
        filename = f"article{idx}.mp3"
        print(f"[{idx}/24] Generating {filename}...")
        communicate = edge_tts.Communicate(text, VOICE)
        primary_path = os.path.join(OUTPUT_DIRS[0], filename)
        await communicate.save(primary_path)
        with open(primary_path, "rb") as f:
            data = f.read()
        for d in OUTPUT_DIRS[1:]:
            with open(os.path.join(d, filename), "wb") as f:
                f.write(data)
        print(f"  -> Saved {filename} ({len(data):,} bytes)")

    print("All 24 matched audio files generated successfully!")

if __name__ == "__main__":
    asyncio.run(generate())
