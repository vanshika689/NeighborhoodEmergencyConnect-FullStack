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
        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "Email Verification - OTP",
            html: `
                <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="background-color: white; border-radius: 8px; padding: 30px; max-width: 400px; margin: 0 auto;">
                        <h2 style="color: #333;">Email Verification</h2>
                        <p>Your OTP code is:</p>
                        <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
                            <h1 style="color: #1976d2; letter-spacing: 5px;">${otp}</h1>
                        </div>
                        <p style="color: #666; font-size: 14px;">This code will expire in 10 minutes.</p>
                        <p style="color: #999; font-size: 12px;">If you didn't request this, please ignore this email.</p>
                    </div>
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
