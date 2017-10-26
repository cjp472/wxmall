var WxParse = require('../wxParse/wxParse.js');
var app = getApp();
Page({
    data: {
        detail: {},
        cartCount:0,
        reviews:[],
        reviewInitFlag:false,
        page: 1,
        endFlag: false,
        loadingFlag: false,
        specificationArray:[],
        indicatorDots: true,
        autoplay: true,
        interval: 3000,
        duration: 1000,
        tabs: ["商品详情",  "商品评价"],
        activeIndex: 0,
        num:1,
        minusStatuses: '',
        flag:0,
        totalPrice:'',
        attrValueList: []
    },
    onLoad: function (param) {
      this.data.productId = param.id;
      var that = this;
      wx.getSystemInfo({
        success: function (scr) {
          var scollHeight = scr.screenHeight - 90;
          app.ajaxJson('/product/detail', { id: param.id}, function (res) {
              var json = res.data;
              if (json.code != 200) {
                  wx.showModal({
                      showCancel: false,
                      title: '错误',
                      content: json.msg ? json.msg : "500",
                  })
                  return;
              }
              that.setData({
                  detail: json.data.productDetail,
                  price: json.data.productDetail.product.price,
                  totalPrice:'',
                  specificationArray: [],
                  specPriceMap: json.data.specPriceMap,
                  cartCount: json.data.cartCount,
                  scollHeight: scollHeight
              });
              
              WxParse.wxParse('article', 'html', json.data.productDetail.product.introduction, that, 5);
              that.distachAttrValue(that.data.detail); //init attr
              
             
              if (!json.data.productDetail.specifications || json.data.productDetail.specifications.length==0){
                 var totalPrice = '';
                 var price = parseFloat(that.data.price);
                 price=price.toFixed(2)
                 var  totalPrice = price* that.data.num;
                 totalPrice = totalPrice.toFixed(2);
                 that.setData({
                   price: price,
                   totalPrice: totalPrice
                 });
              }
              //console.log(that.data.attrValueList);
          });
        }
      })    
      that.getCartCount();  
    },
    getCartCount:function(){
      var that = this;
      app.ajaxJson('/cart/getCartCount', {}, function (res) {
        var json = res.data;
        if (json.code == 200) {
          var cartCount = json.data;
          
          that.setData({
            cartCount: cartCount
          });
        }
      });
    },
    tabClick: function (e) {
      this.setData({
        activeIndex: e.currentTarget.id
      });
      if (e.currentTarget.id == 1 && !this.data.reviewInitFlag) {
        this.data.reviewInitFlag=true;
        this.getReviews(this.data.page);
      }
    },
    swiperchange: function(){
        
    },
    bindViewTap: function (e) {
      var param = e.currentTarget.dataset;
      var id = param["id"];
      var index=param["index"];
      this.setData({
        selected: id,
        firstIndex:index
      });
    },
    onCartClick: function () {
      wx.switchTab({
        url: '/pages/cart/cart'
      })

    },
    setModalStatus: function (e) {
      console.log("设置显示状态，1显示0不显示", e.currentTarget.dataset.status);
      var animation = wx.createAnimation({
        duration: 200,
        timingFunction: "linear",
        delay: 0
      })
      this.animation = animation
      animation.translateY(300).step()
      this.setData({
        animationData: animation.export()
      })
      if (e.currentTarget.dataset.status == 1) {
        this.setData(
          {
            showModalStatus: true
          }
        );
      }
      var flag = e.currentTarget.dataset.flag ;
      this.setData(
        {
          flag: flag
        }
      );
      setTimeout(function () {
        animation.translateY(0).step()
        this.setData({
          animationData: animation
        })
        if (e.currentTarget.dataset.status == 0) {
          this.setData(
            {
              showModalStatus: false
            }
          );
        }
      }.bind(this), 100)
    },
    pay: function (e) {
      var that=this;
      var sflag=true;
      for(var i=0;i<that.data.specificationArray.length;i++){
        if(that.data.specificationArray[i].sfId==''){
          sflag=false;
          break;
        }
      }
      if(!sflag){
        wx.showToast({
          title: '请选择...',
          icon: 'success',
          duration: 1000
        })
        return;
      }
      var quantity = that.data.num;
      var speci = JSON.stringify(that.data.specificationArray);
      var productId = that.data.detail.product.id;
      var flag = that.data.flag;
      if(flag==0){
        var req={};
          req.productId=productId;
          req.quantity=quantity;
          req.speci=speci;
        app.ajaxJson('/cart/addCart', req, function (res) {
          var json = res.data;
          if (json.code != 200) {
            wx.showModal({
              showCancel: false,
              title: '错误',
              content: json.msg ? json.msg : "500",
            })
            return;
          }else{
            var cartCount = json.data;
            wx.showToast({
              title: '加入购物车成功',
              icon: 'success',
              duration: 1000
            });
            that.setData({
              cartCount: cartCount
            });
          }
        });
       
       
      }else if(flag==1){
        var itemsArray = new Array();
        var entity = new Object();
        entity.productId = productId;
        entity.pcount = quantity;
        entity.speci = speci;
        itemsArray.push(entity);
        console.log(JSON.stringify(itemsArray));
        wx.navigateTo({
          url: '../../pay/jiesuan?items=' + JSON.stringify(itemsArray)
        })
      }

      this.setData(
        {
          showModalStatus: false
        }
      );

    },
    bindMinus: function (e) {
      var num = this.data.num;
      // 如果只有1件了，就不允许再减了
      if (num > 1) {
        num--;
      }else {
        return;
      }
      this.setTotalPrice(num);
    },

    bindPlus: function (e) {
    
      var num = this.data.num;
      if(num>=99999){
        return;
      }
      // 自增
      num++;
    
      this.setTotalPrice(num);
     
    },
    bindManual: function (e) {
      var num = parseInt(e.detail.value);
      if (isNaN(num)){
        num=1;
      }
      // 将数值与状态写回
      if(num>0&& num<100000){
         this.setTotalPrice(num);
      }
    },
    setTotalPrice:function(num){
      var totalPrice='';
      if (!isNaN(this.data.price)){
         totalPrice=this.data.price*num;
         totalPrice = totalPrice.toFixed(2);
      }
      this.setData({
        num: num,
        totalPrice: totalPrice
      });

    },
    bindManualTapped: function () {
      // 什么都不做，只为打断跳转
    },
    distachAttrValue: function (detail) {
  
      var attrValueList = this.data.attrValueList;
      var specificationArray = this.data.specificationArray;
      if (detail.specifications && detail.specifications.length>0){
        for (var i = 0; i < detail.specifications.length; i++) {
          var attrKey = "";
          var attrKeyId = "";
          var attrValues = [];
          var attrValuesId = [];
          var attrValueStatus = [];
          attrKey = detail.specifications[i].specification.name;
          attrKeyId = detail.specifications[i].specification.id;

          specificationArray.push({
            sfId: '',
            spvId: ''
          });
          for (var j = 0; j < detail.specifications[i].specificationValues.length; j++) {
            attrValues.push(detail.specifications[i].specificationValues[j].name);
            attrValuesId.push(detail.specifications[i].specificationValues[j].id);
            attrValueStatus.push(true);
          }
          attrValueList.push({
            attrKey: attrKey,
            attrKeyId: attrKeyId,
            attrValues: attrValues,
            attrValuesId: attrValuesId,
            attrValueStatus: attrValueStatus
          });
        }
      }

      this.setData({
        attrValueList: attrValueList
      });
    },
  selectAttrValue: function (e) {
    var attrValueList = this.data.attrValueList;
    var specificationArray = this.data.specificationArray;
    var index = e.currentTarget.dataset.index;//属性索引
    var key = e.currentTarget.dataset.key;
    var value = e.currentTarget.dataset.value;
    var sfId = e.currentTarget.dataset.sfid;
    var spvId = e.currentTarget.dataset.id;
    if (e.currentTarget.dataset.status) {
        
        specificationArray[index].sfId=sfId;
        specificationArray[index].spvId=spvId;
        var spcs=[]
        for (var p = 0; p < specificationArray.length;p++){
          spcs.push(specificationArray[p].spvId);
        }
        var spckey=spcs.join(",");
        var spec = this.data.specPriceMap[spckey];
        var price=''
        var totalPrice = '';
        if(spec){
          price = spec.price;
          totalPrice = spec.price*this.data.num;
        }
        attrValueList[index].selectedValue = value;
        this.setData({
          price:price,
          totalPrice: totalPrice,
          attrValueList: attrValueList
        });
    }
  },
  // 滑动底部加载
  lower: function () {
    // console.log('滑动底部加载', new Date());
    if (!this.data.endFlag && !this.data.loadingFlag) {
      var page = this.data.page + 1;
      this.getReviews(page);

    } else {
      wx.showToast({
        title: '已到最后'
      })
    }
  },
  getReviews: function (page) {
    if (this.data.endFlag) {
      return;
    }
    var that = this;
    that.data.page = page;
    var req = {}
    req.page = page;
    req.productId = that.data.productId;
    that.data.loadingFlag = true;
    app.ajaxJson('/product/getReviewsByPage', req, function (res) {
      that.data.loadingFlag = false;
      var json = res.data;
      if (json.code != 200) {
        that.data.endFlag = true;
        return;
      }
      if (json.data && json.data.list && json.data.list.length > 0) {
        that.setData({
          reviews: that.data.reviews.concat(json.data.list)
        });
      } else {
        that.data.endFlag = true;
      }
      if (json.data.lastPage){
        that.data.endFlag = true;
      }
    }, function () {
      that.data.loadingFlag = false;
    });
  }
 
})