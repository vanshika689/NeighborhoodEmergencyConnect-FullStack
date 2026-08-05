const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST || "smtp.gmail.com",
  port: Number(process.env.EMAIL_PORT) || 587,
  secure: false, 
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS, 
  },
  tls: {
    rejectUnauthorized: false, 
  },
  connectionTimeout: 10000, 
  greetingTimeout: 10000,
  socketTimeout: 10000,
});

const sendOTPEmail = async (email, otp) => {
  try {
    console.log("========== EMAIL DEBUG ==========");
    console.log("Recipient Email:", email);
    console.log("EMAIL_USER:", process.env.EMAIL_USER);
    console.log("EMAIL_PASS exists:", !!process.env.EMAIL_PASS);

    const info = await transporter.sendMail({
      from: `"Neighborhood Connect" <${process.env.EMAIL_USER}>`,
      to: email,
      subject: "Your Verification Code (OTP)",
      html: `
        <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
          <h2 style="color: #4CAF50;">Neighborhood Emergency Connect</h2>
          <p>Thank you for registering! Use the OTP below to verify your email address:</p>
          <div style="background: #f4f4f4; padding: 15px; font-size: 24px; font-weight: bold; letter-spacing: 4px; text-align: center; border-radius: 5px; width: 200px; margin: 20px 0;">
            ${otp}
          </div>
          <p>This code will expire shortly. Do not share this OTP with anyone.</p>
        </div>
      `,
    });

    console.log("OTP Email Sent Successfully! Message ID:", info.messageId);
    return true;

  } catch (error) {
    console.error("EMAIL SENDING ERROR:", error);
    return false;
  }
};

module.exports = { sendOTPEmail };