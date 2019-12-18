/**
 * 工站程序函数包装
 */
var Station = {
    showHtml: "<div class=\"menu\"><div class=\"menu_list\"><ul><li><span onclick=\"SaveAnLamp('ADLX01','发生设备异常')\" class=\"andon_icon andon1\"></span><span>设备异常</span></li><li><span onclick=\"SaveAnLamp('ADLX02','现场发生品质异常')\" class=\"andon_icon andon2\"></span>         <span>品质异常</span></li><li><span onclick=\"SaveAnLamp('ADLX03','现场物料不足')\" class=\"andon_icon andon3\"></span><span>缺料异常</span>         </li><li><span onclick=\"SaveAnLamp('ADLX04','现场呼叫产线主管')\" class=\"andon_icon andon4\"></span><span>呼叫主管</span></li></ul></div></div>",
    Init: function (appendLabelID) {
        console.log(appendLabelID);
        $(appendLabelID).append(this.showHtml);
        $(".set>span").click(function () {
            $(".menu").fadeIn();
            $(".menu_list>ul").fadeIn();
            setTimeout(function () {
                $(".menu_list").addClass("menu_list_show")
            }, 100)
        })

        //给子元素绑定阻止点击事件
        $(".menu_list").click(function (event) {
            event.stopPropagation();//阻止事件冒泡即可
        });

        $(".menu").click(function () {
            $(".menu_list").removeClass("menu_list_show");
            $(".menu_list>ul").fadeOut();
            setTimeout(function () {
                $(".menu").fadeOut();
            }, 100)
        });
        
    }
};