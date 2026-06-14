require("../firebase/firebaseAdmin");
const { getMessaging } = require("firebase-admin/messaging");

const sendNotification = async (token, title, body) => {
    try {
        const response = await getMessaging().send({
            token: token,
            notification: {
                title: title,
                body: body 
            }
        });
        console.log("Notification sent successfully");


    } catch (error) {
        console.log("Notification Error:", error);
    }
};

module.exports = sendNotification;