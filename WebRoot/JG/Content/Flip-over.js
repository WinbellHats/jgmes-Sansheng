// 定义一个分页方法，可多次调用
function paginationNick(opt){
// 参数设置
    var pager={
        paginationBox:'',//class为pagination-nick的div分页容器-- 必填
        mainBox:'',//请求回来的数据内容盒子--必填
        numBtnBox:'',//数字按钮盒子-- 必填
        btnBox:'',//全部按钮盒子 --必填

        pageCount:5,//每页显示几条数据
        numBtnCount:1,//当前页左右两边各多少个数字按钮
        currentPage:0,//当前页码data-page，首屏默认值（也就是数字按钮data-page的值）
        maxCount:0,//ajax请求数据分成的最大页码
        data:[],//ajax请求的数据总数

        ipt:'',//input中的class-- 必填
        goBtn:'',//goto 按钮中的class --必填
        currentBtn:'',//当前点击的按钮的class --必填
    };
    pager = $.extend(pager,opt);  //就是调用该函数时传入的对象合并到pager对象中
    console.log(pager);

    //请求数据页面跳转方法
    function goPage(btn){  //btn是当前点击按钮数字的传入
        //obj为ajax请求数据
        var obj={other:{},value:[11,22,33,44,55,66,77,88,99,0,11,22,33,44,55,66,77,88,99,0,11,22,33,44,55,66,77,88,99,0,11,22,33,44,55,66,77,88,99,0]};
        //将请求回来的数据赋值给pager.data数组中 (array)
        pager.data = obj.value;
        //设置ajax请求回来的全部数据分成的最大页码并且赋值给pager.maxCount
        //当请求回来的数据长度%每页显示的条数如果是整数就直接长度除显示条数，如果有余数就先取整再加1分为第二页
        pager.maxCount=pager.data.length % pager.pageCount ? parseInt(pager.data.length / pager.pageCount) +1 :
            pager.data.length / pager.pageCount;

        // 设置当前页码
        if(!isNaN(btn)){//数字按钮      判断当前点击的按钮是否为数字
            pager.currentPage=parseInt(btn);   //首屏默认的页数

        }else{    //如果当前点击的是非法数字的按钮就做如下的判断
            switch(btn){
                case 'first':pager.currentPage=0;  //如果点击当前的按钮的class为first
                    break;

                case 'prev':if(pager.currentPage>0){   //如果点击的是上一页按钮
                    --pager.currentPage;
                }
                    break;

                case 'next':if(pager.currentPage+1<pager.maxCount){
                    ++pager.currentPage;
                }
                    break;

                case 'last':pager.currentPage=pager.maxCount-1;
                    break;
            }
        }
        //创建数字按钮
        createNumBtn(pager.currentPage);   //调用创建数字按钮的方法

        //赋值给页码跳转输入框的value，表示当前页码
        $('.'+pager.btnBox+' .'+pager.ipt).val(pager.currentPage+1);

        // 显示内容区填充数据的div
        var arr=[];
        var str='';
        //slice方法选指定元素返回，就是返回当前点击的是那个数字按钮
        arr=pager.data.slice(pager.pageCount*pager.currentPage,
            pager.data.length - pager.pageCount*(pager.currentPage+1) > -1 ?
                pager.pageCount*(pager.currentPage+1) : pager.data.length);    //该计算方法是当前显示的条数乘数字按钮中的data-page的值，再用请求回来的总数据减到每页显示的条数
        //遍历点击的数字按钮数组显示当前页显示的数据，也就是内容区域的数据
        arr.forEach(function(v){  //v是当前点击按钮显示的内容
            str+='<div>'+v+'</div>';
        });
        $('.'+pager.mainBox).html(str);   //把显示的内容显示到页面
    }

    //创建非数字按钮和数据内容区
    function createOtherBtn(){
        $('.'+pager.paginationBox).html('<div class="'+pager.btnBox+'">' +
            '<button data-page="first" class="first-btn">首页</button>' +
            '<button data-page="prev" class="prev-btn">上一页</button>' +
            '<span class="'+pager.numBtnBox+'"></span>' +
            '<button data-page="next" class="next-btn">下一页</button>' +
            '<input type="text" placeholder="请输入页码" class="'+pager.ipt+'">' +
            '<button class="'+pager.goBtn+'">确定go</button>' +
            '<button data-page="last" class="last-btn">尾页</button></div>' +
            '<div class="'+pager.mainBox+'"></div>');
        //ipt value变化并赋值给go btn data-page
        $('.'+pager.btnBox+' .'+pager.ipt).change(function(){
            if(!isNaN($(this).val())){//是数字
                if($(this).val() > pager.maxCount){//限制value最大值，跳转尾页
                    $(this).val(pager.maxCount);
                }
                if($(this).val()<1){//限制value最小值，跳转首页
                    $(this).val(1);
                }
            }else{//非数字清空value
                $(this).val('');
            }
            $('.'+pager.btnBox+' .'+pager.goBtn).attr('data-page',$(this).val() ? $(this).val()-1 : '');
        });

        //每个非数字btn绑定请求数据页面跳转方法
        $('.'+pager.btnBox+' button').each(function(i,v){
            console.log(i);
            console.log(v);
            $(this).click(function(){
                //有值且不是上一次的页码时才调用，getAttribute方法返回指定属性名的属性值
                if(v.getAttribute('data-page') && v.getAttribute('data-page') != pager.currentPage){
                    goPage(v.getAttribute('data-page'));
                }
            });
        });
    }
    //创建数字按钮
    function createNumBtn(page){
        //page是页面index从0开始，这里的page加减一要注意
        var str='';
        if(pager.maxCount>pager.numBtnCount*2){//若最大页码数大于等于固定数字按钮总数（pager.numBtnCount*2+1）时
            //此页左边右边各pager.numBtnCount个数字按钮
            if(page-pager.numBtnCount>-1){//此页左边有pager.numBtnCount页 page页码从0开始
                for(var m=pager.numBtnCount;m>0;m--){
                    str+='<button data-page="'+(page-m)+'">'+(page-m+1)+'</button>';
                }
            }else{
                for(var k=0;k<page;k++){
                    str+='<button data-page="'+k+'">'+(k+1)+'</button>';
                }
            }
            str+='<button data-page="'+page+'" class="'+pager.currentBtn+'" disabled="disabled">'+(page+1)+'</button>';//此页
            if(pager.maxCount-page>3){//此页右边有pager.numBtnCount页 page页码从0开始
                for(var j=1;j<pager.numBtnCount+1;j++){
                    str+='<button data-page="'+(page+j)+'">'+(page+j+1)+'</button>';
                }
            }else{
                for(var i=page+1;i<pager.maxCount;i++){
                    str+='<button data-page="'+i+'">'+(i+1)+'</button>';
                }
            }
            /*数字按钮总数小于固定的数字按钮总数pager.numBtnCount*2+1时，
            这个分支，可以省略*/
            if(str.match(/<\/button>/ig).length<pager.numBtnCount*2+1){
                str='';
                if(page<pager.numBtnCount){//此页左边页码少于固定按钮数时
                    for(var n=0;n<page;n++){//此页左边
                        str+='<button data-page="'+n+'">'+(n+1)+'</button>';
                    }
                    str+='<button data-page="'+page+'" class="'+pager.currentBtn+'" disabled="disabled">'+(page+1)+'</button>';//此页
                    for(var x=1;x<pager.numBtnCount*2+1-page;x++){//此页右边
                        str+='<button data-page="'+(page+x)+'">'+(page+x+1)+'</button>';
                    }
                }
                if(pager.maxCount-page-1<pager.numBtnCount){
                    for(var y=pager.numBtnCount*2-(pager.maxCount-page-1);y>0;y--){//此页左边
                        str+='<button data-page="'+(page-y)+'">'+(page-y+1)+'</button>';
                    }
                    str+='<button data-page="'+page+'" class="'+pager.currentBtn+'" disabled="disabled">'+(page+1)+'</button>';//此页
                    for(var z=page+1;z<pager.maxCount;z++){//此页右边
                        str+='<button data-page="'+z+'">'+(z+1)+'</button>';
                    }
                }
            }
        }else{
            str='';
            for(var n=0;n<page;n++){//此页左边
                str+='<button data-page="'+n+'">'+(n+1)+'</button>';
            }
            str+='<button data-page="'+page+'" class="'+pager.currentBtn+'" disabled="disabled">'+(page+1)+'</button>';//此页
            for(var x=1;x<pager.maxCount-page;x++){//此页右边
                str+='<button data-page="'+(page+x)+'">'+(page+x+1)+'</button>';
            }
        }
        $('.'+pager.numBtnBox).html(str);

        //每个btn绑定请求数据页面跳转方法
        $('.'+pager.numBtnBox+' button').each(function(i,v){
            $(this).click(function(){
                goPage(v.getAttribute('data-page'));
            });
        });
        //按钮禁用
        $('.'+pager.btnBox+' button').not('.'+pager.currentBtn).attr('disabled',false);
        if(!page){//首页时
            $('.'+pager.btnBox+' .first-btn').attr('disabled',true);
            $('.'+pager.btnBox+' .prev-btn').attr('disabled','disabled');
        }
        if(page==pager.maxCount-1){//尾页时
            $('.'+pager.btnBox+' .last-btn').attr('disabled',true);
            $('.'+pager.btnBox+' .next-btn').attr('disabled',true);
        }
    }
    //首屏加载
    createOtherBtn();//首屏加载一次非数字按钮即可
    goPage();//请求数据页面跳转满足条件按钮点击都执行，首屏默认跳转到currentPage
}


//调用
paginationNick({
    paginationBox:'pagination-nick',//分页容器--必填
    mainBox:'main-box-nick',//内容盒子--必填
    numBtnBox:'num-box-nick',//数字按钮盒子-- 必填
    btnBox:'btn-box-nick',//按钮盒子 --必填
    ipt:'page-ipt-nick',//input class-- 必填
    goBtn:'go-btn-nick',//go btn class --必填
    currentBtn:'active-nick',//当前按钮class name --必填
});
