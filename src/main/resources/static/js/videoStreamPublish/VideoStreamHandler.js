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
                //alert("'WebSocket' in window1:" + wsurl);
                ws = new WebSocket(wsurl);
                //alert("'WebSocket' in window2");
            } else if ('MozWebSocket' in window) {
                //alert("'MozWebSocket' in window1");
                ws = new MozWebSocket(wsurl);
                //alert("'MozWebSocket' in window2");
            } else {
                console.log("您的浏览器不支持WebSocket。");
                //alert("您的浏览器不支持WebSocket。");
            }
            return ws;
        },
        _init : function () {
            this.pushUrl = "wss://" + win.location.host.replace("8889", "9999") + "/ws/publishVideoStream?"
        },
        _pushStream : function (publishId, onOpenCallBack) {
            const url = this.pushUrl + "publishId=" + publishId + "&userId=" + new Date().getTime();
            if (this._wsHandler[publishId]) {
                throw publishId + '已经初始化过了';
            }
            //alert("_openStream,url=" + url)
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
                //alert("onopen");
                //设置发信息送类型为：ArrayBuffer
                ws.binaryType = "arraybuffer";
                onOpenCallBack();
            }
            ws.onclose = function(e) {
                //alert("onclose=" + e);
                console.log("onclose: closed");
                ws = null;
                //_self._openStream(url, onOpenCallBack); //这个函数在这里之所以再次调用，是为了解决视频传输的过程中突发的连接断开问题。
            }
            ws.onerror = function(e) {
                //alert("onerror=" + e);
                console.log("onerror: error");
                ws = null;
                //_self._openStream(url, onOpenCallBack);//同上面的解释
            }
            //alert("ws finished");
            return ws;
        }
    }

    win.VideoStreamHandler = VideoStreamHandler;

})(window, document);