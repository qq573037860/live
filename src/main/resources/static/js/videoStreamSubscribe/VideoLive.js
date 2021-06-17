(function(win, doc){

    function VideoLive(ops) {
        let options = ops || {};
        this._init(options);
    }

    VideoLive.prototype = {
        _defaultParam : {
            pushUrl : null,
            mediaDataSource : {
                url : null,
                type : 'flv',
                isLive : true,
                withCredentials : false,
                hasAudio : true,
                hasVideo : true,
                duration: 0
            },
            optionalConfig : {
                //enableWorker: true,
                /*seekType: 'range',*/
                enableStashBuffer: false,
                stashInitialSize: undefined,
                isLive: true,
                lazyLoad: false,
                lazyLoadMaxDuration: 0,
                lazyLoadRecoverDuration: 0,
                deferLoadAfterSourceOpen: false,
                fixAudioTimestampGap: false,
            },
            recordJson : {}
        },
        _init : function(options) {
            this._defaultParam.pushUrl = "wss://" + win.location.host.replace("8889","9999") + "/ws/subscribeVideoStream?"
        },
        _live : function(subscribeId, videoDom) {

            if (!this._check(subscribeId, videoDom)) {
                return;
            }

            this._defaultParam.mediaDataSource["url"] = this._defaultParam.pushUrl + "subscribeId=" + subscribeId + "&userId=" + new Date().getTime();
            let player = flvjs.createPlayer(this._defaultParam.mediaDataSource, this._defaultParam.optionalConfig);
            player.attachMediaElement(videoDom);
            player.load();
            this._defaultParam.recordJson[subscribeId] = {
                "player" : player,
                "videoDom" : videoDom
            };

            player.play();

            //避免时间长时间积累缓冲导致延迟越来越高
            setInterval(() => {
                /*let end = videoDom.endTime;
                if (!end) {
                    return;
                }
                let diff = end - videoDom.currentTime;
                if (diff >= 0.5) {
                    videoDom.currentTime = end;
                }*/

                if (player.buffered.length) {
                    let end = player.buffered.end(0);//获取当前buffered值
                    let diff = end - player.currentTime;//获取buffered与currentTime的差值
                    if (diff >= 0.5) {//如果差值大于等于0.5 手动跳帧 这里可根据自身需求来定
                        player.currentTime = end - 0.01;//手动跳帧
                        console.info("change time")
                    }
                }
                console.info((player.buffered.end(0) - player.currentTime) + "-" + player.buffered.end(0) + "-" + player.currentTime)
            }, 2000);
        },
        _destoryPlayer : function(player) {
            player.pause();
            player.unload();
            player.detachMediaElement();
            player.destroy();
        },
        _destoryBySubscribeId : function(subscribeId) {
            let json = this._defaultParam.recordJson[subscribeId];
            this._destoryPlayer(json["player"]);
            delete this._defaultParam.recordJson[subscribeId];
        },
        _check : function (subscribeId, videoDom) {
            if (!this._isDom(videoDom)) {
                throw 'videoDom不是dom对象';
            }

            let json = this._defaultParam.recordJson[subscribeId];
            if (json) {
                console.info(subscribeId + '不能重复订阅,忽略不执行');
                return false;
            } else {
                for (let i in this._defaultParam.recordJson) {
                    if (this._defaultParam.recordJson[i]["videoDom"] == videoDom) {//dom已经被使用过
                        //销毁作用于dom上的player
                        this._destoryPlayer(this._defaultParam.recordJson[i]["player"]);
                        delete this._defaultParam.recordJson[i]["player"];
                        delete this._defaultParam.recordJson[i]["videoDom"];
                    }
                }
            }
            return true;
        },
        _isDom : ( typeof HTMLElement === 'object' ) ?
            function(obj){
                return obj instanceof HTMLElement;
            } :
            function(obj){
                return obj && typeof obj === 'object' && obj.nodeType === 1 && typeof obj.nodeName === 'string';
            },
    }

    window.VideoLive = VideoLive;

})(window, document);