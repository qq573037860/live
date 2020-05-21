/**
 * 忽略
 */
(function(win, doc){

    function $ds(val) {
        let selectType = $ds_f.parse.selectType(val);
    }

    let $ds_f = {
        parse : {
           selectType : function(d) {
               let type = 0;//标签类型
               switch (d.charAt(0)) {
                   case '#':
                       type = 1;//id类型
                       break;
                   case '.':
                       type = 2;//class类型
                       break;
               }
               return type;
           }
        },
        select : function(type, d) {
           let dom;
           if (d > 0) {
               d = d.substr(1);
           }
           switch (type) {
               case 0:
                   dom = document.getElementsByTagName(d);
                   break;
               case 1:
                   dom = document.getElementById(d);
                   break;
               case 2:
                   dom = document.getElementsByClassName(d);
                   break;
           }

        },
        wrapper : function(d) {
            if (d) {
                new Shenjq(Array.prototype.slice.call(document.getElementsByTagName("*")))
            }
        }
    }

    function Shenjq(d) {
        if (d && d instanceof Array) {

        }
    }

    win.$ds = $ds;
    win.Shenjq = Shenjq;

})(window, document);