import asyncio
import os
import edge_tts

ARTICLES = [
    (
        1,
        "Scientists are using artificial intelligence to discover new antibiotics and medicines faster than ever before. Traditional methods of developing medicine take many years and cost millions of dollars. Researchers train computer models on thousands of chemical compounds. The artificial intelligence system then predicts which molecules are most likely to stop dangerous bacteria. In recent tests, researchers identified powerful new compounds that can fight drug-resistant infections. Experts say this technology will revolutionize healthcare and save countless lives in the coming decades."
    ),
    (
        2,
        "Walking is one of the easiest and most effective forms of physical exercise. Doctors recommend taking at least thirty minutes of brisk walking every day. When you walk, blood flow to your brain increases significantly. This helps improve memory, concentration, and emotional well-being. People who walk regularly report lower levels of stress and anxiety. You do not need special equipment to start walking. Just wear comfortable shoes and take a stroll in your neighborhood or a nearby park."
    ),
    (
        3,
        "Coral reefs represent some of the most diverse and fragile marine ecosystems on Earth. Rising ocean temperatures have caused widespread coral bleaching events across tropical regions. When seawater becomes unusually warm, corals expel the colorful algae living inside their tissues. Without this algae, the coral turns completely white and becomes vulnerable to disease and starvation. Marine biologists around the world are developing heat-resilient corals in specialized laboratories. These resilient species are then transplanted back into endangered ocean habitats to restore marine biodiversity."
    ),
    (
        4,
        "My name is Peter and I live in London. Every morning, I wake up at seven o'clock. I drink a warm cup of tea and eat fresh bread with butter. At eight o'clock, I take the red bus to my school. I love meeting my friends in the park during lunchtime. The city is very big, but the people are friendly and welcoming. In the evening, I cook a simple dinner with my family and read a short story before going to sleep."
    ),
    (
        5,
        "Human cognition is frequently susceptible to systematic errors known as cognitive biases. In high-stakes environments, such as medical diagnostics or macroeconomic policy formulation, these subconscious distortions can lead to catastrophic consequences. Confirmation bias causes analysts to disproportionately favor information confirming their preexisting hypotheses while dismissing contrary empirical evidence. Furthermore, the availability heuristic leads individuals to overestimate the probability of events that are readily recalled. Mitigating these psychological blind spots demands structured analytical frameworks and continuous peer review."
    ),
    (
        6,
        "The dialectical interplay between human linguistic vernacular and algorithmic synthesis presents profound epistemological dilemmas. As natural language processing models increasingly generate persuasive prose, they simultaneously homogenize idiosyncratic linguistic traits. This algorithmic curation risks subverting the organic cadence of human communication, establishing an implicit orthodoxy of syntax and semantics. Consequently, linguists and philosophers grapple with whether synthetic rhetoric will inevitably impoverish semantic nuance or serendipitously catalyze novel modes of philosophical discourse."
    ),
]

VOICE = "en-US-ChristopherNeural"

OUTPUT_DIRS = [
    r"c:\Users\XUAN SON\Desktop\Xuan Son Version\PracticeEnglish\app\web\audio",
    r"c:\Users\XUAN SON\Desktop\Xuan Son Version\PracticeEnglish\app\build\web\audio",
    r"c:\Users\XUAN SON\Desktop\Xuan Son Version\PracticeEnglish\backend\src\main\resources\static\audio",
]

async def generate():
    for d in OUTPUT_DIRS:
        os.makedirs(d, exist_ok=True)
    
    for idx, text in ARTICLES:
        filename = f"article{idx}.mp3"
        print(f"Generating {filename}...")
        communicate = edge_tts.Communicate(text, VOICE)
        primary_path = os.path.join(OUTPUT_DIRS[0], filename)
        await communicate.save(primary_path)
        with open(primary_path, "rb") as f:
            data = f.read()
        for d in OUTPUT_DIRS[1:]:
            with open(os.path.join(d, filename), "wb") as f:
                f.write(data)
        print(f"Saved {filename} ({len(data)} bytes) to all dirs")

if __name__ == "__main__":
    asyncio.run(generate())
