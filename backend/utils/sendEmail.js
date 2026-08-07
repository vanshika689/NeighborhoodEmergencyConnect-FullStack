const brevo = require('@getbrevo/brevo');

const sendOTPEmail = async (email, otp) => {
  try {
    console.log("========== SENDING EMAIL VIA BREVO ==========");
    console.log("Recipient Email:", email);
    console.log("BREVO_API_KEY exists:", !!process.env.BREVO_API_KEY);

    // Safe fallback instantiation for Brevo SDK across versions
    const ApiClass = brevo.TransactionalEmailsApi || brevo.default?.TransactionalEmailsApi;
    const SmtpEmailClass = brevo.SendSmtpEmail || brevo.default?.SendSmtpEmail;

    if (!ApiClass || !SmtpEmailClass) {
      throw new Error("Brevo SDK classes could not be resolved from package import.");
    }

    let apiInstance = new ApiClass();
    
    // Set up authentication using the instance API key method
    let apiKey = apiInstance.authentications['apiKey'];
    apiKey.apiKey = process.env.BREVO_API_KEY;

    let sendSmtpEmail = new SmtpEmailClass();
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
    console.log("✅ SUCCESS! Brevo Response:", data);
    return true;

  } catch (error) {
    console.error("❌ BREVO EMAIL ERROR:", error.response?.body || error.message || error);
    return false;
  }
};

module.exports = { sendOTPEmail };