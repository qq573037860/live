(function(win, doc){

    function VideoStreamHandler (ops) {
        let options = ops || {};
        this._init(options);
    }

    VideoStreamHandler.prototype = {
        _wsHandler : {},
        _createWebSocket : function(wsurl) {
            let ws = null;
            if ('WebSocket' in window) {
                ws = new WebSocket(wsurl);
            } else if ('MozWebSocket' in window) {
                ws = new MozWebSocket(wsurl);
            } else {
                console.log("您的浏览器不支持WebSocket。");
            }
            return ws;
        },
        _init : function () {
            this.pushUrl = "wss://" + win.location.host + "/ws/pushStream?"
        },
        _pushStream : function (publishId, onOpenCallBack) {
            const url = this.pushUrl + "publishId=" + publishId;
            if (this._wsHandler[publishId]) {
                throw publishId + '已经初始化过了';
            }
            this._wsHandler[publishId] = this._openStream(url, onOpenCallBack);
        },
        _sendData : function (publishId, data) {
            let handler = this._wsHandler[publishId];
            if (handler) {
                handler.send(new Uint8Array(data));
            }
        },
        _destory : function (publishId) {
            this._wsHandler[publishId].close();
            this._wsHandler[publishId] = null;
        },
        _openStream : function(url, onOpenCallBack) {
            let _self = this;
            let ws = this._createWebSocket(url);

            if (!onOpenCallBack) {
                return;
            }

            ws.onopen = function() {
                //设置发信息送类型为：ArrayBuffer
                ws.binaryType = "arraybuffer";
                onOpenCallBack();
            }
            ws.onclose = function(e) {
                console.log("onclose: closed");
                ws = null;
                _self._openStream(url, onOpenCallBack); //这个函数在这里之所以再次调用，是为了解决视频传输的过程中突发的连接断开问题。
            }
            ws.onerror = function(e) {
                console.log("onerror: error");
                ws = null;
                _self._openStream(url, onOpenCallBack);//同上面的解释
            }

            return ws;
        }
    }

    win.VideoStreamHandler = VideoStreamHandler;

})(window, document);