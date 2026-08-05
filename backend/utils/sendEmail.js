const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 587,
    secure: false,
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASSWORD,
    },
    connectionTimeout: 30000,
    greetingTimeout: 30000,
    socketTimeout: 30000,
    logger: true,
    debug: true
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