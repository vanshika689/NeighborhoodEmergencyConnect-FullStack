const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");
const sendOtpEmail = require("../utils/sendEmail");
const authMiddleware = require("../middleware/authMiddleware");
const Alert = require("../models/Alert");
const sendNotification = require("../utils/sendNotification");

const router = express.Router();

router.post("/register", async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({
                message: "Name, email and password are required"
            });
        }

        let existingUser = await User.findOne({ email });

        if (existingUser && existingUser.isVerified) {
            return res.status(400).json({
                message: "User already exists with this email"
            });
        }

        // 2. Generate a 6-digit OTP & set 10-minute expiry
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

        const hashedPassword = await bcrypt.hash(password, 10);

       if (!existingUser) {
            // New user registration
            existingUser = new User({
                name,
                email,
                password: hashedPassword,
                otp,
                otpExpiresAt,
                isVerified: false
            });
        } else {
            // Unverified user retrying signup: update their details & new OTP
            existingUser.name = name;
            existingUser.password = hashedPassword;
            existingUser.otp = otp;
            existingUser.otpExpiresAt = otpExpiresAt;
        }

        await existingUser.save();

        await sendOtpEmail(email, otp);

       res.status(200).json({
            message: "OTP sent to your email. Please verify to complete registration.",
            email
        });

    } catch (error) {
        console.error("REGISTER ERROR:", error);
        res.status(500).json({
            message: "Server Error: Could not send verification code"
        });
    }
});

router.post("/verify-otp", async (req, res) => {
    try {
        const { email, otp } = req.body;

        if (!email || !otp) {
            return res.status(400).json({
                message: "Email and OTP are required"
            });
        }

        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({
                message: "User not found"
            });
        }

        if (user.isVerified) {
            return res.status(400).json({
                message: "Account is already verified"
            });
        }

        // Check if OTP matches
        if (user.otp !== otp) {
            return res.status(400).json({
                message: "Invalid OTP. Please check your email"
            });
        }

        // Check if OTP has expired
        if (new Date() > user.otpExpiresAt) {
            return res.status(400).json({
                message: "OTP has expired. Please register again to get a new code"
            });
        }

        // Mark verified and clear temporary OTP fields
        user.isVerified = true;
        user.otp = undefined;
        user.otpExpiresAt = undefined;
        await user.save();

        res.status(200).json({
            message: "Email verified successfully! You can now log in."
        });

    } catch (error) {
        console.error("VERIFY OTP ERROR:", error);
        res.status(500).json({
            message: "Server Error"
        });
    }
});

router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;
        
        if (!email || !password) {
            return res.status(400).json({
                message: "Email and password are required"
            });
        }

        const user = await User.findOne({ email });
        
        if (!user) {
            return res.status(400).json({
                message: "User not found"
            });
        }

        const isMatch = await bcrypt.compare(password, user.password);


        if (!isMatch) {
            return res.status(400).json({
                message: "Invalid Credentials"
            });
        }

        if (!user.isVerified) {
            return res.status(403).json({
                message: "Your email is not verified. Please verify your account first."
            });
        }

        const token = jwt.sign(
            { 
                id: user._id,
                role: user.role,
            },
            process.env.JWT_SECRET,
            { expiresIn: "300d" }
        );

        res.status(200).json({
            message: "Login Successful",
            token,
            role: user.role,
            userId: user._id, 
            name: user.name,  
            email: user.email

        });

    } catch (error) {
        console.log("LOGIN ERROR:", error);
        res.status(500).json({
            message: "Server Error"
        });
    }
});

router.post("/resend-otp", async (req, res) => {
    try {
        const { email } = req.body;

        if (!email) {
            return res.status(400).json({ message: "Email is required" });
        }

        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        if (user.isVerified) {
            return res.status(400).json({ message: "Account is already verified" });
        }

        // ⏱️ Rate Limiting: Prevent spamming within 60 seconds
        // (Only allow resend if more than 1 min has passed since otpExpiresAt was set)
        if (user.otpExpiresAt) {
            const timeSinceGenerated = (10 * 60 * 1000) - (user.otpExpiresAt - Date.now());
            if (timeSinceGenerated < 60 * 1000) {
                const waitSeconds = Math.ceil((60 * 1000 - timeSinceGenerated) / 1000);
                return res.status(429).json({
                    message: `Please wait ${waitSeconds} seconds before requesting a new OTP.`
                });
            }
        }

        // Generate fresh OTP & set 10-minute expiry
        const newOtp = Math.floor(100000 + Math.random() * 900000).toString();
        user.otp = newOtp;
        user.otpExpiresAt = new Date(Date.now() + 10 * 60 * 1000);
        await user.save();

        // Dispatch email
        await sendOtpEmail(email, newOtp);

        res.status(200).json({
            message: "New OTP has been sent to your email."
        });

    } catch (error) {
        console.error("RESEND OTP ERROR:", error);
        res.status(500).json({ message: "Server error: Unable to resend OTP" });
    }
});

router.get("/profile", authMiddleware, async (req, res) => {
    try {
        const user = await User.findById(req.user.id).select("-password");
        
        if (!user) {
            return res.status(404).json({
                message: "User not found"
            });
        }

        res.status(200).json({
            message: "Profile fetched Successfully",
            user
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.put("/profile", authMiddleware, async (req, res) => {
    console.log("PROFILE UPDATE BODY:", req.body);
    try {
        const { name, email, profileImage } = req.body;
        const user = await User.findById(req.user.id);
        
        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        if (name) user.name = name;
        if (email) user.email = email;
        if (profileImage) user.profileImage = profileImage;
        
        await user.save();

        res.status(200).json({
            message: "Profile updated successfully",
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.put("/change-password", authMiddleware, async (req, res) => {
    try {
        const { oldPassword, newPassword } = req.body;
        const user = await User.findById(req.user.id);
        
        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }
        
        const isMatch = await bcrypt.compare(oldPassword, user.password);
        if (!isMatch) {
            return res.status(400).json({
                success: false,
                message: "Old password is incorrect"
            });
        }
        
        const salt = await bcrypt.genSalt(10);
        user.password = await bcrypt.hash(newPassword, salt);
        await user.save();
        
        res.status(200).json({
            success: true,
            message: "Password updated successfully"
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

router.patch("/request-volunteer", authMiddleware, async (req, res) => {
    try {
        if (req.user.role === "volunteer") {
            return res.status(403).json({
                message: "You are already a Volunteer"
            });
        }
        if (req.user.role === "admin") {
            return res.status(403).json({
                message: "You are Admin, can't send request"
            });
        }
        
        const userID = req.user.id;
        const USER = await User.findById(userID);
        
        if (!USER) {
            return res.status(404).json({
                message: "User not found"
            });
        }
        if (USER.volunteerRequestStatus === "pending") {
            return res.status(403).json({
                message: "Request is already sent"
            });
        }
        
        if (USER.volunteerRequestStatus === "none" || USER.volunteerRequestStatus === "rejected") {
            USER.volunteerRequestStatus = "pending";
            await USER.save();
            return res.status(200).json({
                message: "Your Approval request is sent, please wait for response"
            });
        }
    } catch (error) {
        console.log(error);
        res.status(500).json({
            message: "Server Error Occurred"
        });
    }
});

router.get("/volunteer-requests", authMiddleware, async (req, res) => {
    try {
        if (req.user.role !== "admin") {
            return res.status(403).json({
                message: "You are not authorized"
            });
        }
        
        const requsers = await User.find({
            volunteerRequestStatus: "pending"
        }).select("_id name email role volunteerRequestStatus");
        
        res.status(200).json({
            message: "Volunteer requests fetched successfully",
            requsers
        });
    } catch (error) {
        console.log(error);
        res.status(500).json({
            message: "Server Error"
        });
    }
});

router.patch("/approve-volunteer/:id", authMiddleware, async (req, res) => {
    try {
        if (req.user.role !== "admin") {
            return res.status(403).json({
                message: "You are not authorized"
            });
        }

        const userId = req.params.id;
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({
                message: "User not found"
            });
        }

        if (user.volunteerRequestStatus !== "pending") {
            return res.status(400).json({
                message: "No pending volunteer request found"
            });
        }

        user.role = "volunteer";
        user.volunteerRequestStatus = "approved";

        await user.save();
        await sendNotification(
            user.fcmToken,
            "Volunteer Request Approved 🎉",
            "Congratulations! Your volunteer request has been approved."
        );

        res.status(200).json({
            message: "Volunteer request approved successfully",
            user
        });
    } catch (error) {
        console.log(error);
        res.status(500).json({
            message: "Server error"
        });
    }
});

router.patch("/reject-volunteer/:id", authMiddleware, async (req, res) => {
    try {
        if (req.user.role !== "admin") {
            return res.status(403).json({
                message: "You are not authorized"
            });
        }

        const userId = req.params.id;
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({
                message: "User not found"
            });
        }

        if (user.volunteerRequestStatus !== "pending") {
            return res.status(400).json({
                message: "No pending volunteer request found"
            });
        }

        user.volunteerRequestStatus = "rejected";
        await user.save();
        
        await sendNotification(
            user.fcmToken,
            "Volunteer Request Update",
            "Unfortunately, your volunteer request was rejected."
        );

        res.status(200).json({
            message: "Volunteer request rejected successfully",
            user
        });
    } catch (error) {
        console.log(error);
        res.status(500).json({
            message: "Server error"
        });
    }
});

router.patch("/save-fcm-token", authMiddleware, async (req, res) => {
    try {
        const { fcmToken } = req.body;

        await User.findByIdAndUpdate(
            req.user.id,
            { fcmToken: fcmToken }
        );

        res.status(200).json({
            message: "FCM token saved"
        });
    } catch (error) {
        console.log(error);
        res.status(500).json({
            message: "Server Error"
        });
    }
});

module.exports = router;