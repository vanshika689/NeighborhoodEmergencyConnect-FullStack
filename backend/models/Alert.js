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
    shortAddress: {
    type: String,
    required: true
},

fullAddress: {
    type: String,
    required: true
},

latitude: {
    type: Number,
    required: true
},

longitude: {
    type: Number,
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
    },
    aiAnalysis: {
        isFake: {
            type: Boolean,
            default: null
        },
        fakeReason: {
            type: String,
            default: ""
        },
        severity: {
            type: String,
            enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL", null],
            default: null
        },
        severityReason: {
            type: String,
            default: ""
        },
        confidence: {
            type: String,
            enum: ["LOW", "MEDIUM", "HIGH",null],
            default: null
        }
    },
    resolvedBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        default: null
    }
    


}, {
    timestamps: true
});

module.exports = mongoose.model("Alert",alertSchema)