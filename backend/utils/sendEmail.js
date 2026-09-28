const sendOtpEmail = async (toEmail, otp) => {
    try {
        const response = await fetch("https://api.brevo.com/v3/smtp/email", {
            method: "POST",
            headers: {
                "accept": "application/json",
                "api-key": process.env.BREVO_API_KEY,
                "content-type": "application/json"
            },
            body: JSON.stringify({
                sender: {
                    name: "CivicGuard Security",
                    email: process.env.EMAIL_FROM
                },
                to: [
                    { email: toEmail }
                ],
                subject: "Your CivicGuard Verification Code",
                htmlContent: `
                    <div style="font-family: Arial, sans-serif; padding: 24px; color: #1A1A2E; background-color: #F8FAFC; border-radius: 12px;">
                        <h2 style="color: #1565C0; margin-bottom: 8px;">CivicGuard Email Verification</h2>
                        <p style="font-size: 14px; color: #64748B;">Please enter this 6-digit code in the app to activate your account:</p>
                        <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1565C0; padding: 14px 0;">
                            ${otp}
                        </div>
                        <p style="font-size: 12px; color: #94A3B8; margin-top: 16px;">This OTP is valid for 10 minutes. If you did not request this, please disregard this email.</p>
                    </div>
                `
            })
        });

        const data = await response.json();

        if (!response.ok) {
            console.error("❌ BREVO API ERROR:", data);
            throw new Error(data.message || "Failed to dispatch email via Brevo API");
        }

        console.log("✅ BREVO: Email dispatched successfully! Message ID:", data.messageId);
        return data;
    } catch (error) {
        console.error("❌ EMAIL SENDING ERROR:", error.message);
        throw error;
    }
};

module.exports = sendOtpEmail;