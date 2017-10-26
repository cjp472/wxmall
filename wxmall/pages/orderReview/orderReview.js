var app = getApp();
Page({
  data: {
    order: null,
    product:null,
    orderId:null,
    productId:null,
    commentType:'3',
    memo:''
  },
  onLoad: function (param) {
    this.data.orderId = param.orderId;
    this.data.productId=param.productId;
    var that = this;
    var req = {};
    req.orderId = param.orderId;
    app.ajaxJson('/order/getOrderInfo', req, function (res) {
      var json = res.data;
      if (json.code != 200) {
        return;
      }
      var product=null;
      var productId = parseInt(that.data.productId);
      if (json.data.orderItems && json.data.orderItems.length>0){
        for (var i = 0;i<json.data.orderItems.length;i++){
          if(json.data.orderItems[i].productId==productId){
            product = json.data.orderItems[i];
            break;

          }
        }
      }

      that.setData({
        product:product,
        order:json.data
      });

    });
  },
  selectCommentType: function (e) {
    var commentType = e.currentTarget.dataset.type;
    this.setData({
      commentType: commentType
    });
  },
  bindTextAreaBlur: function (e) {
    //console.log(e.detail.value);
    this.setData({
      memo: e.detail.value
    })
  },
  saveReview:function(){
    wx.showLoading({
      mask: true,
      title: '正在提交数据，请稍侯...',
    })

    var req = {};
    req.orderId =this.data.orderId;
    req.productId=this.data.productId;
    req.score = this.data.commentType;
    req.content = this.data.memo;
    app.ajaxJson('/order/saveReview', req, function (res) {
      wx.hideLoading();
      var json = res.data;
      if (json.code == 200) {
        wx.navigateBack();
      }else{
        wx.showToast({
          title: '保存评介出错！',
        })
      }
      
    },function(){
      wx.hideLoading();
    });
  }
})  