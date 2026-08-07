const { TransactionalEmailsApi, SendSmtpEmail } = require('@getbrevo/brevo');

const apiInstance = new TransactionalEmailsApi();

apiInstance.setApiKey(
  TransactionalEmailsApi.ApiKeys.apiKey,
  process.env.BREVO_API_KEY
);

const sendOTPEmail = async (email, otp) => {
  try {
    console.log("========== SENDING EMAIL VIA BREVO ==========");
    console.log("Recipient Email:", email);
    console.log("BREVO_API_KEY exists:", !!process.env.BREVO_API_KEY);

    const sendSmtpEmail = new SendSmtpEmail();
    sendSmtpEmail.subject = "Your Verification Code (OTP)";
    sendSmtpEmail.htmlContent = `
      <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
        <h2 style="color: #4CAF50;">Neighborhood Emergency Connect</h2>
        <p>Your verification OTP is:</p>
        <div style="background: #f4f4f4; padding: 15px; font-size: 26px; font-weight: bold; letter-spacing: 5px; text-align: center; width: 200px; border-radius: 5px;">
          ${otp}
        </div>
        <p>This code is valid for 10 minutes. Do not share it with anyone.</p>
      </div>
    `;
    sendSmtpEmail.sender = { 
      name: "Neighborhood Connect", 
      email: process.env.EMAIL_USER || "vanshikaupadhyay325@gmail.com" 
    };
    sendSmtpEmail.to = [{ email: email }];

    const data = await apiInstance.sendTransacEmail(sendSmtpEmail);
    console.log("✅ SUCCESS! Brevo Message ID:", data.body ? data.body.messageId : data.messageId);
    return true;

  } catch (error) {
    console.error("❌ BREVO EMAIL ERROR:", error.response ? error.response.body : error);
    return false;
  }
};

module.exports = { sendOTPEmail };