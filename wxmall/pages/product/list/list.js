var app = getApp();
Page({
  data: {
     inputShowed: false, 
     inputVal: "",//search value
      products:[],
      param:{},
      page:1,
      endFlag:false,
      loadingFlag:false,
      selectCateIndex:0,
      cateName:"全部",
      categorys:[{id:null,name:"全部"}]
  },
  onLoad: function (cate) {
    var that = this;
    wx.getSystemInfo({
      success: function (res) {
          //设置scroll-view 的高度
          var scollHeight = res.screenHeight - 90-65; 
          that.setData({
              scollHeight:scollHeight,
              products:[]
            }
          );
          if (cate && cate.id){
            that.data.param.categId = parseInt(cate.id);
          }
          
          that.getProducts(that.data.page);
          that.getCategroys();

      }
    })      
  },

  getCategroys: function () {
    var that = this;
    app.ajaxJson('/category/list', {}, function (res) {
      var json = res.data;
      if (json.code == 200) {
        var categorys=that.data.categorys.concat(json.data);
        var selectCateIndex=0;
        var cateName = "全部";
        if (that.data.param.categId){
          for(var i=0;i<json.data.length;i++){
            if (that.data.param.categId == categorys[i].id){
              selectCateIndex=i;
              cateName = categorys[i].name;
              break;
            }
          }
        }
        that.setData({
          categorys: categorys,
          cateName: cateName,
          selectCateIndex: selectCateIndex
        });
      }
    });

  },

  getProducts:function(page){
    if(this.data.endFlag){
      return;
    }
    var that = this;
    that.data.page=page;
    var req={}
    req.page=page;
    req.keyword = that.data.inputVal;
    if (that.data.param.categId){
      req.categId = that.data.param.categId;
    }  
    that.data.loadingFlag = true;
    app.ajaxJson('/product/list', req, function (res) {
      that.data.loadingFlag = false;
      var json = res.data;
      if (json.code != 200) {
        that.data.endFlag = true;
        return;
      }
      if(json.data&& json.data.length>0){
        that.setData({
          products: that.data.products.concat( json.data)
        });
      }else {
        that.data.endFlag=true;
      } 
    },function(){
      that.data.loadingFlag = false;
    });
  },

  // 滑动底部加载
  lower: function () {
    // console.log('滑动底部加载', new Date());
    if (!this.data.endFlag && !this.data.loadingFlag) {
       var page = this.data.page + 1;
       this.getProducts(page);
       
    } else {
      wx.showToast({
        title: '已到最后'
      })
    }
  },
  //事件处理函数
  bindViewTap: function(e) {
      var param = e.currentTarget.dataset;
      var id = param["id"];
      wx.navigateTo({
          url: '../detail/detail?id='+id
      })
  },

  //search-----
  search: function () {
    this.setData({
      products: [],
      page: 1,
      endFlag: false,
      loadingFlag: false,
      inputShowed: true
    });
    this.getProducts(1);
  },
  resetSearch: function () {
    this.setData({
      inputVal: "",
      inputShowed: false,
      products: [],
      page: 1,
      endFlag: false,
      loadingFlag: false,
    });
    this.getProducts(1);
  },
  clearInput: function () {
    this.setData({
      inputVal: ""
    });
  },
  inputTyping: function (e) {
    this.setData({
      inputVal: e.detail.value
    });
  },
  goIndex:function(e){
    wx.switchTab({
      url: '/pages/index/index'
    })
  },
  goCart:function(){
    wx.switchTab({
      url: '/pages/cart/cart'
    })
  },
  goUser:function(){
    wx.switchTab({
      url: '/pages/user/user'
    })
  },
  selectCategory: function (e) {
    var index = e.detail.value;
    if(this.data.selectCateIndex==index){
      return;
    }

    this.data.param.categId = this.data.categorys[index].id;
    this.setData({
      selectCateIndex: index,
      products: [],
      page: 1,
      cateName: this.data.categorys[index].name,
      endFlag: false,
      loadingFlag: false
    })
    this.getProducts(1)
  }


})