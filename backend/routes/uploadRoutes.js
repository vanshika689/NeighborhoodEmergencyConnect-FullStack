const express = require("express");
const router = express.Router();
const multer = require("multer");
const imagekit = require("../config/imagekit");

// Store uploaded image in RAM
const storage = multer.memoryStorage();
const upload = multer({ storage });

router.post( "/", upload.single("image"),async (req, res) => {
        try {

            // Check if image was selected
            if (!req.file) {
                return res.status(400).json({
                    message: "No image uploaded"
                });
            }

            // Upload image to ImageKit
            const response = await imagekit.upload({
                file: req.file.buffer,
                fileName: req.file.originalname
            });
             console.log(req.file);

            // Send ImageKit URL back to Android
            res.status(200).json({
                message: "Image uploaded successfully",
                imageUrl: response.url
            });

        } catch (error) {

            console.error(error);

            res.status(500).json({
                message: "Image upload failed"
            });

        }
    }
);

module.exports = router;