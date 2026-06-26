const Groq = require("groq-sdk");

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

const analyzeAlert = async (description, category) => {
    const response = await groq.chat.completions.create({
        model: "llama-3.3-70b-versatile",
        messages: [
            {
                role: "user",
                content: `
                You are an emergency alert analyzer for a civic safety app.
                Analyze this alert and respond in JSON only, no extra text:
                
                Category: ${category}
                Description: "${description}"
                
                Return exactly this JSON:
                {
                  "isFake": true/false,
                  "fakeReason": "one line reason",
                  "severity": "LOW/MEDIUM/HIGH/CRITICAL",
                  "severityReason": "one line reason",
                  "confidence": "LOW/MEDIUM/HIGH"
                }
                `
            }
        ],
        temperature: 0.3, //lesss creative more consistent
    });

    const text = response.choices[0].message.content;
    //Getting Ai reply from response
    try{

    const cleaned = text.replace(/```json|```/g, "").trim();
    return JSON.parse(cleaned)
    ///Ai is wrapping JSON in backticks (ie removing text to a real JavaScript object)
    } catch{
        return {
            isFake: false,
            fakeReason: "Analysis unavailable",
            severity:"MEDIUM",
            severityReason: "Default severity",
            confidence:"LOW"

        };
    }
};

module.exports = analyzeAlert;