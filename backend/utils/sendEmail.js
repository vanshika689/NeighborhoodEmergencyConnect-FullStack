const SibApiV3Sdk = require("@getbrevo/brevo");

const apiInstance = new SibApiV3Sdk.TransactionalEmailsApi();
apiInstance.setApiKey(
    SibApiV3Sdk.TransactionalEmailsApiApiKeys.apiKey,
    process.env.BREVO_API_KEY
);

const sendOtpEmail = async (toEmail, otp) => {
    try {
        const sendSmtpEmail = new SibApiV3Sdk.SendSmtpEmail();

        sendSmtpEmail.subject = "Your CivicGuard Verification Code";
        sendSmtpEmail.sender = { 
            name: "CivicGuard Security", 
            email: process.env.EMAIL_FROM 
        };
        sendSmtpEmail.to = [{ email: toEmail }];
        sendSmtpEmail.htmlContent = `
            <div style="font-family: Arial, sans-serif; padding: 24px; color: #1A1A2E; background-color: #F8FAFC; border-radius: 12px;">
                <h2 style="color: #1565C0; margin-bottom: 8px;">CivicGuard Email Verification</h2>
                <p style="font-size: 14px; color: #64748B;">Please enter this 6-digit code in the app to activate your account:</p>
                <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1565C0; padding: 14px 0;">
                    ${otp}
                </div>
                <p style="font-size: 12px; color: #94A3B8; margin-top: 16px;">This OTP is valid for 10 minutes. If you did not request this, please disregard this email.</p>
            </div>
        `;

        const response = await apiInstance.sendTransacEmail(sendSmtpEmail);
        console.log("✅ BREVO: Email dispatched successfully! Message ID:", response.messageId);
        return response;
    } catch (error) {
        console.error("❌ BREVO ERROR:", error.response?.body || error.message);
        throw error;
    }
};

module.exports = sendOtpEmail;