(function(win, doc){

    function VideoDataRecorder (ops) {
        let options = ops || {};
        this._init(options);
    }

    VideoDataRecorder.prototype = {
        _defaultParam : {
            record : {
                audio: true,
                video:{width:480,height:320}
            },
            videoDom : null,
            URL : window.URL || window.webkitURL,
            dataHandler : null,
            recorder : null,
            stream : null
        },
        _init : function(options) {
            if (!this._isDom(options["videoDom"])) {
                throw 'videoDom不是dom对象';
            }
            this._defaultParam.videoDom = options["videoDom"];
            if (typeof options["dataHandler"] != 'function') {
                throw 'dataHandler不能为空';
            }
            this._defaultParam.dataHandler = options["dataHandler"];
            let recordParam = options["recordParam"];

            let userMedia = this._getUserMedia();
            if (userMedia) {
                userMedia(recordParam ? this._extend({}, [this._defaultParam.record, recordParam]) : this._defaultParam.record)
                    .then(this._event.success.bind(this)).catch(this._event.error);
            } else {
                alert("你的浏览器不支持访问用户媒体设备");
            }
            if (!MediaRecorder) {
                alert("你的浏览器不支持录制视频");
            }
        },
        _isDom : ( typeof HTMLElement === 'object' ) ?
                function(obj){
                    return obj instanceof HTMLElement;
                } :
                function(obj){
                    return obj && typeof obj === 'object' && obj.nodeType === 1 && typeof obj.nodeName === 'string';
                },
        _getUserMedia : function() {
            let userMedia = null;
            if(navigator.mediaDevices.getUserMedia){
                //最新标准API
                userMedia = navigator.mediaDevices.getUserMedia;
            } else if (navigator.webkitGetUserMedia){
                //webkit内核浏览器
                userMedia = navigator.webkitGetUserMedia;
            } else if (navigator.mozGetUserMedia){
                //Firefox浏览器
                userMedia = navagator.mozGetUserMedia;
            } else if (navigator.getUserMedia){
                //旧版API
                userMedia = navigator.getUserMedia;
            }
            return userMedia;
        },
        _start : function() {
            //视频数据录制
            if (this._defaultParam.dataHandler) {
                let recorder = new MediaRecorder(this._defaultParam.stream, {mimeType: 'video/webm'});
                this._defaultParam.recorder = recorder;
                recorder.start(10);
                recorder.addEventListener('dataavailable', function (e) {
                    if (e.data.size > 0) {
                        let fr = new FileReader();
                        fr.addEventListener("loadend", function (d) {
                            //reader.result是一个含有视频数据流的Blob对象
                            try {
                                if (d.target.result.byteLength > 0) {//加这个判断，是因为有很多数据是空的，这个没有必要发到后台服务器，减轻网络开销，提升性能吧。
                                    this._defaultParam.dataHandler(d.target.result);
                                }
                            } catch (e) {
                                for (var i in e) {
                                    console.error('异常,key:' + i + ",value:" + e[i]);
                                }
                            }
                        });

                        fr.readAsArrayBuffer(e.data);
                    }
                });
            }
        },
        _pause : function() {
            if (this._defaultParam.recorder) {
                this._defaultParam.recorder.pause();
            }
        },
        _resume : function() {
            if (this._defaultParam.recorder) {
                this._defaultParam.recorder.resume();
            }
        },
        _destory : function() {
            if (this._defaultParam.recorder) {
                this._defaultParam.recorder.stop();
                this._defaultParam.recorder = null;
            }
        },
        _event : {
            success : function (stream) {
                let $dom = this._defaultParam.videoDom;
                //将流喂给video标签
                try {
                    $dom.src = this._defaultParam.URL.createObjectURL(stream);
                } catch (e) {
                    $dom.srcObject = stream;
                }
                $dom.play();
                this._defaultParam.stream = stream;
            },
            error : function (e) {
                alert("访问用户媒体设备失败：" + e.name + "；" + e.message);
            }
        },
        _extend : function (des, src, override) {
            if(src instanceof Array){
                for(let i = 0, len = src.length; i < len; i++)
                    extend(des, src[i], override);
            }
            for(let i in src){
                if(override || !(i in des)){
                    des[i] = src[i];
                }
            }
            return des;
        }
    }

    win.VideoDataRecorder = VideoDataRecorder;

})(window, document);