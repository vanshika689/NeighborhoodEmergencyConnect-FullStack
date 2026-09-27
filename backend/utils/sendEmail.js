require("dotenv").config();
const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  host: "smtp.gmail.com",
  port: 465,
  secure: true,
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

const sendOtpEmail = async (email, otp) => {
  const mailOptions = {
    from: `"CivicGuard" <${process.env.EMAIL_USER}>`,
    to: email,
    subject: `${otp} is your CivicGuard verification code`,
    // Providing text alongside html improves email deliverability
    text: `Your CivicGuard verification code is ${otp}. This code is valid for 10 minutes. Please do not share this code with anyone.`,
    html: `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Email Verification</title>
      </head>
      <body style="font-family: Arial, sans-serif; background-color: #f7f9fc; margin: 0; padding: 24px;">
        <div style="max-width: 500px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 24px; border: 1px solid #e2e8f0;">
          <h2 style="color: #1976D2; margin-top: 0;">CivicGuard Verification</h2>
          <p style="color: #4a5568; font-size: 15px; line-height: 1.5;">
            Thank you for registering. Use the code below to verify your email address:
          </p>
          <div style="text-align: center; margin: 28px 0;">
            <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a202c; background-color: #edf2f7; padding: 12px 24px; border-radius: 6px; display: inline-block;">
              ${otp}
            </span>
          </div>
          <p style="color: #718096; font-size: 13px; line-height: 1.4;">
            This verification code will expire in <strong>10 minutes</strong>. If you did not request this email, please ignore it.
          </p>
          <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
          <p style="color: #a0aec0; font-size: 12px; text-align: center; margin-bottom: 0;">
            © CivicGuard Security Team
          </p>
        </div>
      </body>
      </html>
    `,
  };

  return await transporter.sendMail(mailOptions);
};

module.exports = sendOtpEmail;