const express = require("express");
const User = require("../models/User");
const sendNotification = require("../utils/sendNotification");
const analyzeAlert = require("../utils/analyzeAlert");

const Alert = require("../models/Alert");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

///function to calculate dis between 2 GPS coordinates
function getDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth radius in meters
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c; // distance in meters
}
router.post("/", authMiddleware, async (req, res) => {

    try {

        const { title, description,shortAddress,fullAddress,latitude,longitude ,imageUrl} = req.body;
        let aiAnalysis = null
        try{
            aiAnalysis = await analyzeAlert(description, title);
        } catch(aiError){
            console.log("AI analysis failed, continuing without it:", aiError.message);
        }

        const alert = new Alert({
        title,
        description,
        shortAddress,
        fullAddress,
        latitude,
        longitude,
        imageUrl,
        createdBy: req.user.id,
         aiAnalysis
        });

        await alert.save();
        const populatedAlert = await Alert.findById(alert._id)
        .populate("createdBy","name email")
        .populate("responders","name email")
        .populate("resolvedBy","name email");
               const users = await User.find({
             _id: { $ne: req.user.id },
              fcmToken: { $ne: "" }
});

  for (const user of users) {
            await sendNotification(
                user.fcmToken,
                "🚨 New Emergency Alert",
                `${title} reported at ${shortAddress}`
            );

        }

        res.status(201).json({
            message: "Alert Created Successfully",
            alert : populatedAlert
        });

    } catch(error){

        console.log(error);

        res.status(500).json({
            message: "Server Error"
        });

    }

});

router.get("/", async (req,res)=>{
    try{
        const alerts = await Alert.find()
        .sort({createdAt: -1})
        .populate("createdBy","name email")
        .populate("responders","name email")
        .populate("resolvedBy","name email"); 
        res.status(200).json({
            message:"Alert fetched Successfully",
            alerts
        });


    } catch(error){
        console.error(error);
        res.status(500).json({
            message: "Server error"
        });
    }


});

router.patch("/:id/resolve", authMiddleware, async (req, res) => {
    try {
        const alertID = req.params.id;
        const { status, volunteerLat, volunteerLng } = req.body;

        const alert = await Alert.findById(alertID);

        if(!alert){
            return res.status(404).json({
                message: "Alert not found"
            });
        }

        if(
            alert.createdBy.toString() !== req.user.id &&
            req.user.role !== "volunteer" && req.user.role!=="admin"
        ){
            return res.status(403).json({
                message: "You are not authorized to update this alert"
            });
        }

        if(status === "resolved") {
            if(!volunteerLat || !volunteerLng){
                return res.status(400).json({
                    message: "Your location is required to resolve this alert"
                });
            }

            const distance = getDistance(
                parseFloat(volunteerLat), parseFloat(volunteerLng),
                alert.latitude, alert.longitude
            );

            console.log(`Volunteer distance from alert: ${distance} meters`);

            if(distance > 200){
                return res.status(403).json({
                    message: `You must be at the alert location to resolve it. You are ${Math.round(distance)} meters away.`,
                    distance: Math.round(distance)
                });
            }

            alert.status = "resolved";
            alert.resolvedBy = req.user.id;
             await alert.save();
            const updatedAlert = await Alert.findById(alertID)
            .populate("createdBy","name email")
            .populate("responders","name email")
            .populate("resolvedBy","name email");
            return res.status(200).json({
                message: "Alert Resolved Successfully ✅",
                alert: updatedAlert
            });

        } else if(status === "active"){
            alert.status = "active";
            await alert.save();
            return res.status(200).json({
                message: "Alert Status Updated Successfully",
                alert
            });
        } else {
            return res.status(400).json({
                message: "Invalid Status"
            });
        }

    } catch(error){
        console.log(error);
        res.status(500).json({
            message: "Server Error"
        });
    }
});

router.get("/my-alerts", authMiddleware, async (req, res) => {

    try {

        const alerts = await Alert.find({
            createdBy: req.user.id
        })
        .populate("createdBy", "name email")
        .populate("responders", "name email").sort({
            createdAt: -1
        })
        .populate("resolvedBy","name email"); 

        res.status(200).json({
            message: "Alerts fetched successfully",
            alerts
        });

    } catch(error) {

        console.log(error);

        res.status(500).json({
            message: "Server error occurred"
        });

    }

});

router.get("/my-responses", authMiddleware,async(req,res)=>{
   try {
    const alerts = await Alert.find({
        responders: req.user.id
    }).populate("createdBy", "name email")
    .populate("responders", "name email");
    res.status(200).json({
        message: "Alerts fetched successfully",
        alerts
    });

   } catch(error){
    console.log(error);
    res.status(500).json({
        message: "Server error occured"
    });
    
   }
});

router.get("/:id", async(req,res)=>{
    try{
    const alertid = req.params.id;
    const alert = await Alert.findById(alertid)
    .populate("createdBy","name email")
    .populate("responders","name email")
    .populate("resolvedBy","name email");
    if(!alert){
        return res.status(404).json({
            message: "Alert not found"
        });
    }
    res.status(200).json({
        message:"Alert fetched Successfully",
        alert
    });
    } catch (error){
        res.status(500).json({
            message: "Server error"
        });
    }
});

router.patch("/:id", authMiddleware, async (req, res) => {

    try {
        const alertID = req.params.id;

        const { status } = req.body;

        const alert = await Alert.findById(alertID);

   
        if(!alert){
            return res.status(404).json({
                message: "Alert not found"
            });
        }
  
        if(
            alert.createdBy.toString() !== req.user.id &&
            req.user.role !== "admin"
        ){
            return res.status(403).json({
                message: "You are not authorized to update this alert"
            });
        }

        if(status==="resolved"  || status==="active"){
            alert.status = status;
        // save updated alert
        await alert.save();
         res.status(200).json({
            message: "Alert Status Updated Successfully",
            alert
        });
        } else {
            return res.status(400).json({
                message: "Invalid Status"
            });

        }
    } catch(error){
         console.log(error);

        res.status(500).json({
            message: "Server Error"
        });

    }

});

router.post("/:id/respond", authMiddleware, async(req,res)=>{
    try{
          const alertid = req.params.id;
          const alert = await Alert.findById(alertid);
          if(!alert){
            return res.status(404).json({
                message: "Alert not found"
            });
        }
        if(alert.status==="resolved"){
            return res.status(403).json({
                message: "This Alert has been already resolved"
            });
        }
        
        if(req.user.role!=="volunteer" && req.user.role!=="admin"){
            return res.status(403).json({
                message: "Only volunteers and admins can respond to alerts "
            });
        }
        if(alert.responders.includes(req.user.id)){
            return res.status(400).json({
                message: "You already responded to this alert"
            });
        }
        
        alert.responders.push(req.user.id);
        await alert.save();
         const updatedAlert = await Alert.findById(alertid)
        .populate("createdBy", "name email")
        .populate("responders", "name email");

        const creator = await User.findById(alert.createdBy);
        const responder = await User.findById(req.user.id);
        
        await sendNotification(
        creator.fcmToken,
        "Someone Responded to Your Alert 🚑",
         `${responder.name} has responded to your emergency alert.`
);
        
        res.status(200).json({
            message: "Volunteer Response Registered Successfully",
            alert: updatedAlert
        });
    } catch(error){
        console.log(error);
        res.status(500).json({
            message: "Server Error"
        });

    }
});

router.delete("/:id", authMiddleware, async(req,res)=>{
    try{
    const alertid = req.params.id;
    const alert = await Alert.findById(alertid);
    if(!alert){
        return res.status(404).json({
            message: "Alert not found"
        });
    }
    if(alert.createdBy.toString() === req.user.id || req.user.role === "admin"){
       await Alert.findByIdAndDelete(alertid);
        res.status(200).json({
            message: "Alert deleted Successfully"
        });
    } else {
        return res.status(403).json({
            message: "You are nor authorized to delete this Alert"
        });
    }

   } catch (error){
    console.log(error);
    res.status(500).json({
        message:"Server error occurred"
    });

   }
});

module.exports = router;