const { Resend } = require('resend');

// Initialize Resend with your API Key
const resend = new Resend(process.env.RESEND_API_KEY);

const sendOTPEmail = async (email, otp) => {
  try {
    console.log("========== SENDING EMAIL VIA RESEND ==========");
    console.log("Recipient:", email);

    const data = await resend.emails.send({
      from: 'onboarding@resend.dev', // Default testing domain provided by Resend
      to: email,
      subject: 'Neighborhood Connect <onboarding@resend.dev>',
      html: `
        <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
          <h2 style="color: #4CAF50;">Neighborhood Emergency Connect</h2>
          <p>Your verification OTP is:</p>
          <div style="background: #f4f4f4; padding: 15px; font-size: 26px; font-weight: bold; letter-spacing: 5px; text-align: center; width: 200px; border-radius: 5px;">
            ${otp}
          </div>
          <p>This OTP is valid for 10 minutes.</p>
        </div>
      `,
    });

    console.log("SUCCESS! Resend Email ID:", data.id);
    return true;
  } catch (error) {
    console.error("RESEND EMAIL ERROR:", error);
    return false;
  }
};

module.exports = { sendOTPEmail };