let publishJson = {};
function publish(publishId) {
    if (!publishJson[publishId]) {
        let streamHandler = new window.VideoStreamHandler();
        streamHandler._pushStream(publishId, function(){
            let recorder = new window.VideoDataRecorder({
                "videoDom": document.getElementById("myVideo"),
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
        videoLive._live(subscribeId, document.getElementById("subscribeVideo"));
        subscribeJson[subscribeId] = videoLive;
    }
}

function bindEvent() {

    document.getElementById("publish_id_confirm").addEventListener('click', function(){
        let publishId = document.getElementById("publish_id").value;
        if (publishId) {
            document.getElementById("identifyWindow").style.display = "none";
            document.getElementById("video-area").style.display = "block";
            publish(publishId);
        }
    }, false);

    document.getElementById("subscribe_id_confirm").addEventListener('click', function(){
        let subscribeId = document.getElementById("subscribe_id").value;
        if (subscribeId) {
            document.getElementById("subscribe_id_confirm").style.display = "none";
            document.getElementById("subscribe_id").style.display = "none";
            subscribe(subscribeId);
        }
    }, false);
}

/**
 *  初始化方法
 */
function loaded() {
    bindEvent();
    document.removeEventListener('DOMContentLoaded', loaded, false);
}
document.addEventListener('DOMContentLoaded', loaded);
