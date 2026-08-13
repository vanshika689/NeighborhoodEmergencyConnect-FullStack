const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");
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

        if (existingUser) {
            return res.status(400).json({
                message: "User already exists with this email"
            });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const user = new User({
            name,
            email,
            password: hashedPassword,
        });

        await user.save();

        res.status(201).json({
            message: "Register Successfully",
            email,
        });

    } catch (error) {
        console.error("REGISTER ERROR:", error);
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