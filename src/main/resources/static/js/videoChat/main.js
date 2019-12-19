
let publishJson = {};

function publish(publishId) {
    if (!publishJson[publishId]) {
        let streamHandler = new window.VideoStreamHandler();
        streamHandler._pushStream(publishId, function(){
            let recorder = new window.VideoDataRecorder({
                "videoDom": document.getElementById(""),
                "dataHandler": function (data) {
                    streamHandler._sendData(publishId, data);
                }
            });
            publishJson[publishId]["recorder"] = recorder;
        });
        publishJson[publishId] = {
            "streamHandler" : streamHandler
        };
    }
}

let subscribeJson = {};

function subscribe(subscribeId) {
    if (!subscribeJson[subscribeId]) {
        let videoLive = new window.VideoLive();
        videoLive._live(subscribeId, document.getElementById(""));
        subscribeJson[subscribeId] = videoLive;
    }
}


function bindEvent() {

    

}

function loaded() {



    document.removeEventListener('DOMContentLoaded', loaded, false);
}
document.addEventListener('DOMContentLoaded', loaded);
