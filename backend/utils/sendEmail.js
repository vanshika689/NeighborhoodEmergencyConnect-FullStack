const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST || 'smtp.gmail.com', 
  port: Number(process.env.EMAIL_PORT) || 587,
  secure: false, 
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS, 
  },
  connectionTimeout: 10000, 
  greetingTimeout: 5000,
  socketTimeout: 10000,
});
const sendOTPEmail = async (email, otp) => {
    try {

        console.log("========== EMAIL DEBUG ==========");
        console.log("EMAIL_USER:", process.env.EMAIL_USER);
        console.log("EMAIL_PASSWORD exists:", !!process.env.EMAIL_PASSWORD);

        await transporter.verify();
        console.log("SMTP Verify Success");

        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "Email Verification - OTP",
            html: `<h2>Your OTP is ${otp}</h2>`
        });

        console.log("OTP email sent successfully");
        return true;

    } catch (error) {
        console.error("EMAIL ERROR:", error);
        return false;
    }
};

module.exports = { sendOTPEmail };