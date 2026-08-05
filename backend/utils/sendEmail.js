const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
    service: "gmail",
    
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASSWORD,
    }
});

const sendOTPEmail = async (email, otp) => {
    try {

        console.log("EMAIL_USER:", process.env.EMAIL_USER);
        console.log("EMAIL_PASSWORD exists:", !!process.env.EMAIL_PASSWORD);

        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "Email Verification - OTP",
            html: `
                <div>
                    <h2>Email Verification</h2>
                    <p>Your OTP is <b>${otp}</b></p>
                </div>
            `
        });

        console.log("OTP email sent successfully");
        return true;

    } catch (error) {
        console.error("Email sending error:", error);
        return false;
    }
};
module.exports = { sendOTPEmail };