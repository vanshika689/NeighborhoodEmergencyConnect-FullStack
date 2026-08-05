const { Resend } = require('resend');

const resend = new Resend(process.env.RESEND_API_KEY);

const sendOTPEmail = async (email, otp) => {
  try {
    console.log("========== SENDING EMAIL VIA RESEND ==========");
    console.log("Recipient Email:", email);
    console.log("RESEND_API_KEY exists:", !!process.env.RESEND_API_KEY);

    const { data, error } = await resend.emails.send({
      from: 'Neighborhood Connect <onboarding@resend.dev>',
      to: email, 
      subject: 'Email Verification - OTP',
      html: `
        <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
          <h2 style="color: #4CAF50;">Neighborhood Emergency Connect</h2>
          <p>Your verification OTP is:</p>
          <div style="background: #f4f4f4; padding: 15px; font-size: 26px; font-weight: bold; letter-spacing: 5px; text-align: center; width: 200px; border-radius: 5px;">
            ${otp}
          </div>
          <p>This code is valid for 10 minutes. Do not share it with anyone.</p>
        </div>
      `,
    });

    if (error) {
      console.error("❌ RESEND API REJECTED EMAIL:", error);
      return false;
    }

    console.log("✅ SUCCESS! Resend Email ID:", data.id);
    return true;

  } catch (err) {
    console.error("❌ RESEND SDK EXCEPTION:", err);
    return false;
  }
};

module.exports = { sendOTPEmail };