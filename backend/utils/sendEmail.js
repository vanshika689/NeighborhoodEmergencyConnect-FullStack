const SibApiV3Sdk = require('@getbrevo/brevo');

const sendEmail = async (toEmail, otp) => {
    try {
        let apiInstance = new SibApiV3Sdk.TransactionalEmailsApi();
        
        // Configure API key authorization
        let apiKey = apiInstance.authentications['apiKey'];
        apiKey.apiKey = process.env.BREVO_API_KEY;

        let sendSmtpEmail = new SibApiV3Sdk.SendSmtpEmail();

        sendSmtpEmail.subject = "Your OTP Code for Neighborhood Emergency Connect";
        sendSmtpEmail.htmlContent = `<p>Hello,</p><p>Your OTP for registration is: <strong>${otp}</strong></p>`;
        sendSmtpEmail.sender = { "name": "Neighborhood Emergency Connect", "email": process.env.SENDER_EMAIL };
        sendSmtpEmail.to = [{ "email": toEmail }];

        const data = await apiInstance.sendTransacEmail(sendSmtpEmail);
        console.log('✅ Email sent successfully:', data);
        return true;
    } catch (error) {
        console.error('❌ Detailed Brevo Error:', error);
        throw error;
    }
};

module.exports = sendEmail;