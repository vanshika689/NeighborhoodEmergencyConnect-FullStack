const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");
const Alert = require("../models/Alert");
const authMiddleware = require("../middleware/authMiddleware");


const router = express.Router();

router.get("/", authMiddleware, async (req,res)=>{
    try{

    
    if(req.user.role==="admin"){
        const totalusers = await User.countDocuments();
        const totalcitizens = await User.countDocuments({
            role: "citizen"
        });
        const totalvolunteers = await User.countDocuments({
            role: "volunteer"
        });
        const totalalerts = await Alert.countDocuments();
        const totalactivealerts = await Alert.countDocuments({
            status: "active"
        });
        const totalresolvedalerts = await Alert.countDocuments({
            status: "resolved"
        });
        const totalvolunteerreq = await User.countDocuments({
            volunteerRequestStatus: {
                $in: ["pending", "approved", "rejected"]
            }
        });
       const totalapproved= await User.countDocuments({
            volunteerRequestStatus: "approved", 
        });
         const totalrejected = await User.countDocuments({
            volunteerRequestStatus: "rejected", 
        });
         const totalpending = await User.countDocuments({
            volunteerRequestStatus: "pending", 

        });


         return res.status(200).json({
            role: "admin",
            message: "Details fetched Successfully",
            totalusers: totalusers,
            totalCitizens: totalcitizens,
            totalvolunteers: totalvolunteers,
            totalalerts : totalalerts,
            totalactivealerts : totalactivealerts,
            totalresolvedalerts: totalresolvedalerts,
            totalvolunteerreq : totalvolunteerreq ,
            totalapproved : totalapproved,
            totalrejected : totalrejected,
            totalpending : totalpending 
         });
    } else if(req.user.role==="volunteer"){
       const totalactivealerts = await Alert.countDocuments({
            status: "active"
        });
        const totalalerts = await Alert.countDocuments();
         const totalresolvedalerts = await Alert.countDocuments({
            status: "resolved"
        });
        const id = req.user.id;
        const alertsrespondedbyme = await Alert.countDocuments({
            responders: id
        });
         const activealertsrespondedbyme = await Alert.countDocuments({
            responders: id,
            status : "active"
        });
         const resolvedalertsrespondedbyme = await Alert.countDocuments({
            responders: id,
            status : "resolved"
        });
        
        res.status(200).json({
            role :"volunteer",
            message: "Details fetched Successfully",
            totalalerts: totalalerts,
            totalactivealerts : totalactivealerts,
            totalresolvedalerts : totalresolvedalerts,
            alertsrespondedbyme :  alertsrespondedbyme,
            activealertsrespondedbyme: activealertsrespondedbyme,
            resolvedalertsrespondedbyme : resolvedalertsrespondedbyme
        });

    } else if(req.user.role==="citizen"){
        const totalalertsbyme = await Alert.countDocuments({
            createdBy : req.user.id
        });
        const totalactivealertsbyme = await Alert.countDocuments({
            createdBy: req.user.id,
            status: "active"
        });
        const totalresolvedalerts = await Alert.countDocuments({
            createdBy : req.user.id,
            status: "resolved"
        });
        res.status(200).json({
            role: "citizen",
            message: "Details fetched Successfully",
           totalalertsbyme:  totalalertsbyme,
           totalactivealertsbyme :totalactivealertsbyme,
            totalresolvedalerts : totalresolvedalerts
        });
    }
    } catch(error){
        console.log(error);
        res.status(500).json({
            message: "Server Error"
        });
    }


});

module.exports = router;




