const mongoose = require("mongoose");
const alertSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true
    },
    description: {
        type: String,
        required: true
    },
    location: {
        type: String,
        required: true
    },
    status: {
        type: String,
        enum: ["active", "resolved"],
        default: "active"
    },
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User"
    },
    responders: [
        {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User"
        },
       
    ],
    imageUrl : {
        type: String,
        default: ""
    }

}, {
    timestamps: true
});

module.exports = mongoose.model("Alert",alertSchema)