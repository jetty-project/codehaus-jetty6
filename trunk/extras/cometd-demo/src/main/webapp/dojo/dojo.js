/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo=="undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 5779 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_d,_e){
if(typeof _d!="string"){
return dojo.global();
}
if(_d.indexOf(".")==-1){
return dojo.evalProp(_d,dojo.global(),_e);
}
var _f=dojo.parseObjPath(_d,dojo.global(),_e);
if(_f){
return dojo.evalProp(_f.prop,_f.obj,_e);
}
return null;
};
dojo.errorToString=function(_10){
if(!dj_undef("message",_10)){
return _10.message;
}else{
if(!dj_undef("description",_10)){
return _10.description;
}else{
return _10;
}
}
};
dojo.raise=function(_11,_12){
if(_12){
_11=_11+": "+dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_11);
}
}
catch(e){
}
throw _12||Error(_11);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_14){
return dj_global.eval?dj_global.eval(_14):eval(_14);
}
dojo.unimplemented=function(_15,_16){
var _17="'"+_15+"' not implemented";
if(_16!=null){
_17+=" "+_16;
}
dojo.raise(_17);
};
dojo.deprecated=function(_18,_19,_1a){
var _1b="DEPRECATED: "+_18;
if(_19){
_1b+=" "+_19;
}
if(_1a){
_1b+=" -- will be removed in version: "+_1a;
}
dojo.debug(_1b);
};
dojo.render=(function(){
function vscaffold(_1c,_1d){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1c};
for(var i=0;i<_1d.length;i++){
tmp[_1d[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _20={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_20;
}else{
for(var _21 in _20){
if(typeof djConfig[_21]=="undefined"){
djConfig[_21]=_20[_21];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _24=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _25={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_26,_27){
this.modulePrefixes_[_26]={name:_26,value:_27};
},moduleHasPrefix:function(_28){
var mp=this.modulePrefixes_;
return Boolean(mp[_28]&&mp[_28].value);
},getModulePrefix:function(_2a){
if(this.moduleHasPrefix(_2a)){
return this.modulePrefixes_[_2a].value;
}
return _2a;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2b in _25){
dojo.hostenv[_2b]=_25[_2b];
}
})();
dojo.hostenv.loadPath=function(_2c,_2d,cb){
var uri;
if(_2c.charAt(0)=="/"||_2c.match(/^\w+:/)){
uri=_2c;
}else{
uri=this.getBaseScriptUri()+_2c;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2d?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2d,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _32=this.getText(uri,null,true);
if(!_32){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_32="("+_32+")";
}
var _33=dj_eval(_32);
if(cb){
cb(_33);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_35,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_35,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3c){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3c]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_3f){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_3f]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_41){
var _42=_41.split(".");
for(var i=_42.length;i>0;i--){
var _44=_42.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_44)){
_42[0]="../"+_42[0];
}else{
var _45=this.getModulePrefix(_44);
if(_45!=_44){
_42.splice(0,i,_45);
break;
}
}
}
return _42;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_46,_47,_48){
if(!_46){
return;
}
_48=this._global_omit_module_check||_48;
var _49=this.findModule(_46,false);
if(_49){
return _49;
}
if(dj_undef(_46,this.loading_modules_)){
this.addedToLoadingCount.push(_46);
}
this.loading_modules_[_46]=1;
var _4a=_46.replace(/\./g,"/")+".js";
var _4b=_46.split(".");
var _4c=this.getModuleSymbols(_46);
var _4d=((_4c[0].charAt(0)!="/")&&!_4c[0].match(/^\w+:/));
var _4e=_4c[_4c.length-1];
var ok;
if(_4e=="*"){
_46=_4b.slice(0,-1).join(".");
while(_4c.length){
_4c.pop();
_4c.push(this.pkgFileName);
_4a=_4c.join("/")+".js";
if(_4d&&_4a.charAt(0)=="/"){
_4a=_4a.slice(1);
}
ok=this.loadPath(_4a,!_48?_46:null);
if(ok){
break;
}
_4c.pop();
}
}else{
_4a=_4c.join("/")+".js";
_46=_4b.join(".");
var _50=!_48?_46:null;
ok=this.loadPath(_4a,_50);
if(!ok&&!_47){
_4c.pop();
while(_4c.length){
_4a=_4c.join("/")+".js";
ok=this.loadPath(_4a,_50);
if(ok){
break;
}
_4c.pop();
_4a=_4c.join("/")+"/"+this.pkgFileName+".js";
if(_4d&&_4a.charAt(0)=="/"){
_4a=_4a.slice(1);
}
ok=this.loadPath(_4a,_50);
if(ok){
break;
}
}
}
if(!ok&&!_48){
dojo.raise("Could not load '"+_46+"'; last tried '"+_4a+"'");
}
}
if(!_48&&!this["isXDomain"]){
_49=this.findModule(_46,false);
if(!_49){
dojo.raise("symbol '"+_46+"' is not defined after loading '"+_4a+"'");
}
}
return _49;
};
dojo.hostenv.startPackage=function(_51){
var _52=String(_51);
var _53=_52;
var _54=_51.split(/\./);
if(_54[_54.length-1]=="*"){
_54.pop();
_53=_54.join(".");
}
var _55=dojo.evalObjPath(_53,true);
this.loaded_modules_[_52]=_55;
this.loaded_modules_[_53]=_55;
return _55;
};
dojo.hostenv.findModule=function(_56,_57){
var lmn=String(_56);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_57){
dojo.raise("no loaded module named '"+_56+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_59){
var _5a=_59["common"]||[];
var _5b=_59[dojo.hostenv.name_]?_5a.concat(_59[dojo.hostenv.name_]||[]):_5a.concat(_59["default"]||[]);
for(var x=0;x<_5b.length;x++){
var _5d=_5b[x];
if(_5d.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5d);
}else{
dojo.hostenv.loadModule(_5d);
}
}
};
dojo.require=function(){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(){
var _5e=arguments[0];
if((_5e===true)||(_5e=="common")||(_5e&&dojo.render[_5e].capable)){
var _5f=[];
for(var i=1;i<arguments.length;i++){
_5f.push(arguments[i]);
}
dojo.require.apply(dojo,_5f);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_61,_62){
return dojo.hostenv.setModulePrefix(_61,_62);
};
dojo.setModulePrefix=function(_63,_64){
dojo.deprecated("dojo.setModulePrefix(\""+_63+"\", \""+_64+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_63,_64);
};
dojo.exists=function(obj,_66){
var p=_66.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_69){
return _69?_69.toLowerCase():dojo.locale;
};
dojo.hostenv.searchLocalePath=function(_6a,_6b,_6c){
_6a=dojo.hostenv.normalizeLocale(_6a);
var _6d=_6a.split("-");
var _6e=[];
for(var i=_6d.length;i>0;i--){
_6e.push(_6d.slice(0,i).join("-"));
}
_6e.push(false);
if(_6b){
_6e.reverse();
}
for(var j=_6e.length-1;j>=0;j--){
var loc=_6e[j]||"ROOT";
var _72=_6c(loc);
if(_72){
break;
}
}
};
dojo.hostenv.preloadLocalizations=function(){
var _73;
if(_73){
dojo.registerModulePath("nls","nls");
function preload(_74){
_74=dojo.hostenv.normalizeLocale(_74);
dojo.hostenv.searchLocalePath(_74,true,function(loc){
for(var i=0;i<_73.length;i++){
if(_73[i]==loc){
dojo.require("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _77=djConfig.extraLocale||[];
for(var i=0;i<_77.length;i++){
preload(_77[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_79,_7a,_7b){
dojo.hostenv.preloadLocalizations();
var _7c=[_79,"nls",_7a].join(".");
var _7d=dojo.hostenv.findModule(_7c);
if(_7d){
if(djConfig.localizationComplete&&_7d._built){
return;
}
var _7e=dojo.hostenv.normalizeLocale(_7b).replace("-","_");
var _7f=_7c+"."+_7e;
if(dojo.hostenv.findModule(_7f)){
return;
}
}
_7d=dojo.hostenv.startPackage(_7c);
var _80=dojo.hostenv.getModuleSymbols(_79);
var _81=_80.concat("nls").join("/");
var _82;
dojo.hostenv.searchLocalePath(_7b,false,function(loc){
var _84=loc.replace("-","_");
var _85=_7c+"."+_84;
var _86=false;
if(!dojo.hostenv.findModule(_85)){
dojo.hostenv.startPackage(_85);
var _87=[_81];
if(loc!="ROOT"){
_87.push(loc);
}
_87.push(_7a);
var _88=_87.join("/")+".js";
_86=dojo.hostenv.loadPath(_88,null,function(_89){
var _8a=function(){
};
_8a.prototype=_82;
_7d[_84]=new _8a();
for(var j in _89){
_7d[_84][j]=_89[j];
}
});
}else{
_86=true;
}
if(_86&&_7d[_84]){
_82=_7d[_84];
}else{
_7d[_84]=_82;
}
});
};
(function(){
var _8c=djConfig.extraLocale;
if(_8c){
if(!_8c instanceof Array){
_8c=[_8c];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_90){
req(m,b,_90);
if(_90){
return;
}
for(var i=0;i<_8c.length;i++){
req(m,b,_8c[i]);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _92=document.location.toString();
var _93=_92.split("?",2);
if(_93.length>1){
var _94=_93[1];
var _95=_94.split("&");
for(var x in _95){
var sp=_95[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _99=document.getElementsByTagName("script");
var _9a=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_99.length;i++){
var src=_99[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_9a);
if(m){
var _9e=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_9e+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_9e;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_9e;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _a6=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_a6>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_a6+6,_a6+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _a8=window["document"];
var tdi=_a8["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _ac=null;
var _ad=null;
try{
_ac=new XMLHttpRequest();
}
catch(e){
}
if(!_ac){
for(var i=0;i<3;++i){
var _af=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_ac=new ActiveXObject(_af);
}
catch(e){
_ad=e;
}
if(_ac){
dojo.hostenv._XMLHTTP_PROGIDS=[_af];
break;
}
}
}
if(!_ac){
return dojo.raise("XMLHTTP not available",_ad);
}
return _ac;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_b1,_b2){
if(!_b1){
this._blockAsync=true;
}
var _b3=this.getXmlhttpObject();
function isDocumentOk(_b4){
var _b5=_b4["status"];
return Boolean((!_b5)||((200<=_b5)&&(300>_b5))||(_b5==304));
}
if(_b1){
var _b6=this,timer=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_b3.onreadystatechange=function(){
if(timer){
gbl.clearTimeout(timer);
timer=null;
}
if(_b6._blockAsync||(xhr&&xhr._blockAsync)){
timer=gbl.setTimeout(function(){
_b3.onreadystatechange.apply(this);
},10);
}else{
if(4==_b3.readyState){
if(isDocumentOk(_b3)){
_b1(_b3.responseText);
}
}
}
};
}
_b3.open("GET",uri,_b1?true:false);
try{
_b3.send(null);
if(_b1){
return null;
}
if(!isDocumentOk(_b3)){
var err=Error("Unable to load "+uri+" status:"+_b3.status);
err.status=_b3.status;
err.responseText=_b3.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_b2)&&(!_b1)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _b3.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_b9){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_b9);
}else{
try{
var _ba=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_ba){
_ba=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_b9));
_ba.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_b9+"</div>");
}
catch(e2){
window.status=_b9;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_bc,_bd,fp,_bf){
var _c0=_bc["on"+_bd]||function(){
};
_bc["on"+_bd]=function(){
fp.apply(_bc,arguments);
_c0.apply(_bc,arguments);
};
return true;
}
function dj_load_init(e){
var _c2=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_c2!="domcontentloaded"&&_c2!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _c3=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_c3();
dojo.hostenv.modulesLoaded();
}else{
dojo.addOnLoad(_c3);
}
}
if(document.addEventListener){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
document.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
});
dojo.hostenv.makeWidgets=function(){
var _c5=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_c5=_c5.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_c5=_c5.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_c5.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _c6=new dojo.xml.Parse();
if(_c5.length>0){
for(var x=0;x<_c5.length;x++){
var _c8=document.getElementById(_c5[x]);
if(!_c8){
continue;
}
var _c9=_c6.parseElement(_c8,null,true);
dojo.widget.getParser().createComponents(_c9);
}
}else{
if(djConfig.parseWidgets){
var _c9=_c6.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_c9);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_ce,_cf){
dj_currentContext=_ce;
dj_currentDocument=_cf;
};
dojo._fireCallback=function(_d0,_d1,_d2){
if((_d1)&&((typeof _d0=="string")||(_d0 instanceof String))){
_d0=_d1[_d0];
}
return (_d1?_d0.apply(_d1,_d2||[]):_d0());
};
dojo.withGlobal=function(_d3,_d4,_d5,_d6){
var _d7;
var _d8=dj_currentContext;
var _d9=dj_currentDocument;
try{
dojo.setContext(_d3,_d3.document);
_d7=dojo._fireCallback(_d4,_d5,_d6);
}
finally{
dojo.setContext(_d8,_d9);
}
return _d7;
};
dojo.withDoc=function(_da,_db,_dc,_dd){
var _de;
var _df=dj_currentDocument;
try{
dj_currentDocument=_da;
_de=dojo._fireCallback(_db,_dc,_dd);
}
finally{
dj_currentDocument=_df;
}
return _de;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _e0=false;
var _e1=false;
var _e2=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_e0=true;
}else{
if(typeof this["load"]=="function"){
_e1=true;
}else{
if(window.widget){
_e2=true;
}
}
}
var _e3=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_e3.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_e0)&&(!_e2)){
_e3.push("browser_debug.js");
}
var _e4=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_e4=djConfig["baseLoaderUri"];
}
for(var x=0;x<_e3.length;x++){
var _e6=_e4+"src/"+_e3[x];
if(_e0||_e1){
load(_e6);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_e6+"'></scr"+"ipt>");
}
catch(e){
var _e7=document.createElement("script");
_e7.src=_e6;
document.getElementsByTagName("head")[0].appendChild(_e7);
}
}
}
})();
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_e8,_e9){
if(typeof _e9!="function"){
dojo.raise("dojo.inherits: superclass argument ["+_e9+"] must be a function (subclass: ["+_e8+"']");
}
_e8.prototype=new _e9();
_e8.prototype.constructor=_e8;
_e8.superclass=_e9.prototype;
_e8["super"]=_e9.prototype;
};
dojo.lang._mixin=function(obj,_eb){
var _ec={};
for(var x in _eb){
if((typeof _ec[x]=="undefined")||(_ec[x]!=_eb[x])){
obj[x]=_eb[x];
}
}
if(dojo.render.html.ie&&(typeof (_eb["toString"])=="function")&&(_eb["toString"]!=obj["toString"])&&(_eb["toString"]!=_ec["toString"])){
obj.toString=_eb.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_ef){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_f1,_f2){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_f1.prototype,arguments[i]);
}
return _f1;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_f4,_f5,_f6,_f7){
if(!dojo.lang.isArrayLike(_f4)&&dojo.lang.isArrayLike(_f5)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var _f8=_f4;
_f4=_f5;
_f5=_f8;
}
var _f9=dojo.lang.isString(_f4);
if(_f9){
_f4=_f4.split("");
}
if(_f7){
var _fa=-1;
var i=_f4.length-1;
var end=-1;
}else{
var _fa=1;
var i=0;
var end=_f4.length;
}
if(_f6){
while(i!=end){
if(_f4[i]===_f5){
return i;
}
i+=_fa;
}
}else{
while(i!=end){
if(_f4[i]==_f5){
return i;
}
i+=_fa;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_fd,_fe,_ff){
return dojo.lang.find(_fd,_fe,_ff,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_100,_101){
return dojo.lang.find(_100,_101)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
if(!it){
return false;
}
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
dojo.lang.isString=function(it){
return (it instanceof String||typeof it=="string");
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction()&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.lang.array");
dojo.lang.has=function(obj,name){
try{
return (typeof obj[name]!="undefined");
}
catch(e){
return false;
}
};
dojo.lang.isEmpty=function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _10f=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_10f++;
break;
}
}
return (_10f==0);
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
};
dojo.lang.map=function(arr,obj,_113){
var _114=dojo.lang.isString(arr);
if(_114){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_113)){
_113=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_113){
var _115=obj;
obj=_113;
_113=_115;
}
}
if(Array.map){
var _116=Array.map(arr,_113,obj);
}else{
var _116=[];
for(var i=0;i<arr.length;++i){
_116.push(_113.call(obj,arr[i]));
}
}
if(_114){
return _116.join("");
}else{
return _116;
}
};
dojo.lang.reduce=function(arr,_119,obj,_11b){
var _11c=_119;
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_11c=_11b.call(ob,_11c,val);
});
return _11c;
};
dojo.lang.forEach=function(_11f,_120,_121){
if(dojo.lang.isString(_11f)){
_11f=_11f.split("");
}
if(Array.forEach){
Array.forEach(_11f,_120,_121);
}else{
if(!_121){
_121=dj_global;
}
for(var i=0,l=_11f.length;i<l;i++){
_120.call(_121,_11f[i],i,_11f);
}
}
};
dojo.lang._everyOrSome=function(_123,arr,_125,_126){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[(_123)?"every":"some"](arr,_125,_126);
}else{
if(!_126){
_126=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _128=_125.call(_126,arr[i],i,arr);
if((_123)&&(!_128)){
return false;
}else{
if((!_123)&&(_128)){
return true;
}
}
}
return (_123)?true:false;
}
};
dojo.lang.every=function(arr,_12a,_12b){
return this._everyOrSome(true,arr,_12a,_12b);
};
dojo.lang.some=function(arr,_12d,_12e){
return this._everyOrSome(false,arr,_12d,_12e);
};
dojo.lang.filter=function(arr,_130,_131){
var _132=dojo.lang.isString(arr);
if(_132){
arr=arr.split("");
}
if(Array.filter){
var _133=Array.filter(arr,_130,_131);
}else{
if(!_131){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_131=dj_global;
}
var _133=[];
for(var i=0;i<arr.length;i++){
if(_130.call(_131,arr[i],i,arr)){
_133.push(arr[i]);
}
}
}
if(_132){
return _133.join("");
}else{
return _133;
}
};
dojo.lang.unnest=function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
};
dojo.lang.toArray=function(_138,_139){
var _13a=[];
for(var i=_139||0;i<_138.length;i++){
_13a.push(_138[i]);
}
return _13a;
};
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_13d){
var _13e=window,argsStart=2;
if(!dojo.lang.isFunction(func)){
_13e=func;
func=_13d;
_13d=arguments[2];
argsStart++;
}
if(dojo.lang.isString(func)){
func=_13e[func];
}
var args=[];
for(var i=argsStart;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_13e,args);
},_13d);
};
dojo.lang.clearTimeout=function(_141){
dojo.global().clearTimeout(_141);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_149,_14a,_14b){
with(dojo.parseObjPath(_149,_14a,_14b)){
return dojo.evalProp(prop,obj,_14b);
}
};
dojo.lang.setObjPathValue=function(_14c,_14d,_14e,_14f){
if(arguments.length<4){
_14f=true;
}
with(dojo.parseObjPath(_14c,_14e,_14f)){
if(obj&&(_14f||(prop in obj))){
obj[prop]=_14d;
}
}
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_150,_151){
var fcn=(dojo.lang.isString(_151)?_150[_151]:_151)||function(){
};
return function(){
return fcn.apply(_150,arguments);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_153,_154,_155){
var nso=(_154||dojo.lang.anon);
if((_155)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_153){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_153;
return ret;
};
dojo.lang.forward=function(_159){
return function(){
return this[_159].apply(this,arguments);
};
};
dojo.lang.curry=function(ns,func){
var _15c=[];
ns=ns||dj_global;
if(dojo.lang.isString(func)){
func=ns[func];
}
for(var x=2;x<arguments.length;x++){
_15c.push(arguments[x]);
}
var _15e=(func["__preJoinArity"]||func.length)-_15c.length;
function gather(_15f,_160,_161){
var _162=_161;
var _163=_160.slice(0);
for(var x=0;x<_15f.length;x++){
_163.push(_15f[x]);
}
_161=_161-_15f.length;
if(_161<=0){
var res=func.apply(ns,_163);
_161=_162;
return res;
}else{
return function(){
return gather(arguments,_163,_161);
};
}
}
return gather([],_15c,_15e);
};
dojo.lang.curryArguments=function(ns,func,args,_169){
var _16a=[];
var x=_169||0;
for(x=_169;x<args.length;x++){
_16a.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[ns,func].concat(_16a));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_170,_171){
if(!farr.length){
if(typeof _171=="function"){
_171();
}
return;
}
if((typeof _170=="undefined")&&(typeof cb=="number")){
_170=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_170){
_170=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_170,_171);
},_170);
};
dojo.provide("dojo.event.common");
dojo.event=new function(){
this.canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_173){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false};
switch(args.length){
case 0:
return;
case 1:
return;
case 2:
ao.srcFunc=args[0];
ao.adviceFunc=args[1];
break;
case 3:
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isFunction(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
var _176=dl.nameAnonFunc(args[2],ao.adviceObj,_173);
ao.adviceFunc=_176;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _176=dl.nameAnonFunc(args[0],ao.srcObj,_173);
ao.srcFunc=_176;
ao.adviceObj=args[1];
ao.adviceFunc=args[2];
}
}
}
}
break;
case 4:
if((dl.isObject(args[0]))&&(dl.isObject(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isString(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isFunction(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
var _176=dl.nameAnonFunc(args[1],dj_global,_173);
ao.srcFunc=_176;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _176=dl.nameAnonFunc(args[3],dj_global,_173);
ao.adviceObj=dj_global;
ao.adviceFunc=_176;
}else{
if(dl.isObject(args[1])){
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=dj_global;
ao.adviceFunc=args[3];
}else{
if(dl.isObject(args[2])){
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
ao.srcObj=ao.adviceObj=ao.aroundObj=dj_global;
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
ao.aroundFunc=args[3];
}
}
}
}
}
}
break;
case 6:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundFunc=args[5];
ao.aroundObj=dj_global;
break;
default:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundObj=args[5];
ao.aroundFunc=args[6];
ao.once=args[7];
ao.delay=args[8];
ao.rate=args[9];
ao.adviceMsg=args[10];
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _176=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_173);
ao.aroundFunc=_176;
}
if(dl.isFunction(ao.srcFunc)){
ao.srcFunc=dl.getNameInObj(ao.srcObj,ao.srcFunc);
}
if(dl.isFunction(ao.adviceFunc)){
ao.adviceFunc=dl.getNameInObj(ao.adviceObj,ao.adviceFunc);
}
if((ao.aroundObj)&&(dl.isFunction(ao.aroundFunc))){
ao.aroundFunc=dl.getNameInObj(ao.aroundObj,ao.aroundFunc);
}
if(!ao.srcObj){
dojo.raise("bad srcObj for srcFunc: "+ao.srcFunc);
}
if(!ao.adviceObj){
dojo.raise("bad adviceObj for adviceFunc: "+ao.adviceFunc);
}
if(!ao.adviceFunc){
dojo.debug("bad adviceFunc for srcFunc: "+ao.srcFunc);
dojo.debugShallow(ao);
}
return ao;
}
this.connect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.connect(ao);
}
ao.srcFunc="onkeypress";
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _178={};
for(var x in ao){
_178[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_178.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_178));
});
return mjps;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
if(ao.adviceFunc){
var mjp2=dojo.event.MethodJoinPoint.getForMethod(ao.adviceObj,ao.adviceFunc);
}
mjp.kwAddAdvice(ao);
return mjp;
};
this.log=function(a1,a2){
var _180;
if((arguments.length==1)&&(typeof a1=="object")){
_180=a1;
}else{
_180={srcObj:a1,srcFunc:a2};
}
_180.adviceFunc=function(){
var _181=[];
for(var x=0;x<arguments.length;x++){
_181.push(arguments[x]);
}
dojo.debug("("+_180.srcObj+")."+_180.srcFunc,":",_181.join(", "));
};
this.kwConnect(_180);
};
this.connectBefore=function(){
var args=["before"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectAround=function(){
var args=["around"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.once=true;
return this.connect(ao);
};
this._kwConnectImpl=function(_188,_189){
var fn=(_189)?"disconnect":"connect";
if(typeof _188["srcFunc"]=="function"){
_188.srcObj=_188["srcObj"]||dj_global;
var _18b=dojo.lang.nameAnonFunc(_188.srcFunc,_188.srcObj,true);
_188.srcFunc=_18b;
}
if(typeof _188["adviceFunc"]=="function"){
_188.adviceObj=_188["adviceObj"]||dj_global;
var _18b=dojo.lang.nameAnonFunc(_188.adviceFunc,_188.adviceObj,true);
_188.adviceFunc=_18b;
}
return dojo.event[fn]((_188["type"]||_188["adviceType"]||"after"),_188["srcObj"]||dj_global,_188["srcFunc"],_188["adviceObj"]||_188["targetObj"]||dj_global,_188["adviceFunc"]||_188["targetFunc"],_188["aroundObj"],_188["aroundFunc"],_188["once"],_188["delay"],_188["rate"],_188["adviceMsg"]||false);
};
this.kwConnect=function(_18c){
return this._kwConnectImpl(_18c,false);
};
this.disconnect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(!ao.adviceFunc){
return;
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.disconnect(ao);
}
ao.srcFunc="onkeypress";
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
return mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
};
this.kwDisconnect=function(_18f){
return this._kwConnectImpl(_18f,true);
};
};
dojo.event.MethodInvocation=function(_190,obj,args){
this.jp_=_190;
this.object=obj;
this.args=[];
for(var x=0;x<args.length;x++){
this.args[x]=args[x];
}
this.around_index=-1;
};
dojo.event.MethodInvocation.prototype.proceed=function(){
this.around_index++;
if(this.around_index>=this.jp_.around.length){
return this.jp_.object[this.jp_.methodname].apply(this.jp_.object,this.args);
}else{
var ti=this.jp_.around[this.around_index];
var mobj=ti[0]||dj_global;
var meth=ti[1];
return mobj[meth].call(mobj,this);
}
};
dojo.event.MethodJoinPoint=function(obj,_198){
this.object=obj||dj_global;
this.methodname=_198;
this.methodfunc=this.object[_198];
this.before=[];
this.after=[];
this.around=[];
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_19a){
if(!obj){
obj=dj_global;
}
if(!obj[_19a]){
obj[_19a]=function(){
};
if(!obj[_19a]){
dojo.raise("Cannot set do-nothing method on that object "+_19a);
}
}else{
if((!dojo.lang.isFunction(obj[_19a]))&&(!dojo.lang.isAlien(obj[_19a]))){
return null;
}
}
var _19b=_19a+"$joinpoint";
var _19c=_19a+"$joinpoint$method";
var _19d=obj[_19b];
if(!_19d){
var _19e=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_19e=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_19b,_19c,_19a]);
}
}
var _19f=obj[_19a].length;
obj[_19c]=obj[_19a];
_19d=obj[_19b]=new dojo.event.MethodJoinPoint(obj,_19c);
obj[_19a]=function(){
var args=[];
if((_19e)&&(!arguments.length)){
var evt=null;
try{
if(obj.ownerDocument){
evt=obj.ownerDocument.parentWindow.event;
}else{
if(obj.documentElement){
evt=obj.documentElement.ownerDocument.parentWindow.event;
}else{
if(obj.event){
evt=obj.event;
}else{
evt=window.event;
}
}
}
}
catch(e){
evt=window.event;
}
if(evt){
args.push(dojo.event.browser.fixEvent(evt,this));
}
}else{
for(var x=0;x<arguments.length;x++){
if((x==0)&&(_19e)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _19d.run.apply(_19d,args);
};
obj[_19a].__preJoinArity=_19f;
}
return _19d;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _1a5=[];
for(var x=0;x<args.length;x++){
_1a5[x]=args[x];
}
var _1a7=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _1a9=marr[0]||dj_global;
var _1aa=marr[1];
if(!_1a9[_1aa]){
dojo.raise("function \""+_1aa+"\" does not exist on \""+_1a9+"\"");
}
var _1ab=marr[2]||dj_global;
var _1ac=marr[3];
var msg=marr[6];
var _1ae;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _1a9[_1aa].apply(_1a9,to.args);
}};
to.args=_1a5;
var _1b0=parseInt(marr[4]);
var _1b1=((!isNaN(_1b0))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _1b4=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event.canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_1a7(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_1ac){
_1ab[_1ac].call(_1ab,to);
}else{
if((_1b1)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_1a9[_1aa].call(_1a9,to);
}else{
_1a9[_1aa].apply(_1a9,args);
}
},_1b0);
}else{
if(msg){
_1a9[_1aa].call(_1a9,to);
}else{
_1a9[_1aa].apply(_1a9,args);
}
}
}
};
if(this.before.length>0){
dojo.lang.forEach(this.before.concat(new Array()),_1a7);
}
var _1b7;
if(this.around.length>0){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_1b7=mi.proceed();
}else{
if(this.methodfunc){
_1b7=this.object[this.methodname].apply(this.object,args);
}
}
if(this.after.length>0){
dojo.lang.forEach(this.after.concat(new Array()),_1a7);
}
return (this.methodfunc)?_1b7:null;
},getArr:function(kind){
var arr=this.after;
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
arr=this.before;
}else{
if(kind=="around"){
arr=this.around;
}
}
return arr;
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"]);
},addAdvice:function(_1bc,_1bd,_1be,_1bf,_1c0,_1c1,once,_1c3,rate,_1c5){
var arr=this.getArr(_1c0);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_1bc,_1bd,_1be,_1bf,_1c3,rate,_1c5];
if(once){
if(this.hasAdvice(_1bc,_1bd,_1c0,arr)>=0){
return;
}
}
if(_1c1=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_1c8,_1c9,_1ca,arr){
if(!arr){
arr=this.getArr(_1ca);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _1c9=="object")?(new String(_1c9)).toString():_1c9;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_1c8)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_1d0,_1d1,_1d2,once){
var arr=this.getArr(_1d2);
var ind=this.hasAdvice(_1d0,_1d1,_1d2,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_1d0,_1d1,_1d2,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_1d6){
if(!this.topics[_1d6]){
this.topics[_1d6]=new this.TopicImpl(_1d6);
}
return this.topics[_1d6];
};
this.registerPublisher=function(_1d7,obj,_1d9){
var _1d7=this.getTopic(_1d7);
_1d7.registerPublisher(obj,_1d9);
};
this.subscribe=function(_1da,obj,_1dc){
var _1da=this.getTopic(_1da);
_1da.subscribe(obj,_1dc);
};
this.unsubscribe=function(_1dd,obj,_1df){
var _1dd=this.getTopic(_1dd);
_1dd.unsubscribe(obj,_1df);
};
this.destroy=function(_1e0){
this.getTopic(_1e0).destroy();
delete this.topics[_1e0];
};
this.publishApply=function(_1e1,args){
var _1e1=this.getTopic(_1e1);
_1e1.sendMessage.apply(_1e1,args);
};
this.publish=function(_1e3,_1e4){
var _1e3=this.getTopic(_1e3);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_1e3.sendMessage.apply(_1e3,args);
};
};
dojo.event.topic.TopicImpl=function(_1e7){
this.topicName=_1e7;
this.subscribe=function(_1e8,_1e9){
var tf=_1e9||_1e8;
var to=(!_1e9)?dj_global:_1e8;
dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_1ec,_1ed){
var tf=(!_1ed)?_1ec:_1ed;
var to=(!_1ed)?null:_1ec;
dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.destroy=function(){
dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage").disconnect();
};
this.registerPublisher=function(_1f0,_1f1){
dojo.event.connect(_1f0,_1f1,this,"sendMessage");
};
this.sendMessage=function(_1f2){
};
};
dojo.provide("dojo.event.browser");
dojo._ie_clobber=new function(){
this.clobberNodes=[];
function nukeProp(node,prop){
try{
node[prop]=null;
}
catch(e){
}
try{
delete node[prop];
}
catch(e){
}
try{
node.removeAttribute(prop);
}
catch(e){
}
}
this.clobber=function(_1f5){
var na;
var tna;
if(_1f5){
tna=_1f5.all||_1f5.getElementsByTagName("*");
na=[_1f5];
for(var x=0;x<tna.length;x++){
if(tna[x]["__doClobber__"]){
na.push(tna[x]);
}
}
}else{
try{
window.onload=null;
}
catch(e){
}
na=(this.clobberNodes.length)?this.clobberNodes:document.all;
}
tna=null;
var _1f9={};
for(var i=na.length-1;i>=0;i=i-1){
var el=na[i];
try{
if(el&&el["__clobberAttrs__"]){
for(var j=0;j<el.__clobberAttrs__.length;j++){
nukeProp(el,el.__clobberAttrs__[j]);
}
nukeProp(el,"__clobberAttrs__");
nukeProp(el,"__doClobber__");
}
}
catch(e){
}
}
na=null;
};
};
if(dojo.render.html.ie){
dojo.addOnUnload(function(){
dojo._ie_clobber.clobber();
try{
if((dojo["widget"])&&(dojo.widget["manager"])){
dojo.widget.manager.destroyAll();
}
}
catch(e){
}
try{
window.onload=null;
}
catch(e){
}
try{
window.onunload=null;
}
catch(e){
}
dojo._ie_clobber.clobberNodes=[];
});
}
dojo.event.browser=new function(){
var _1fd=0;
this.clean=function(node){
if(dojo.render.html.ie){
dojo._ie_clobber.clobber(node);
}
};
this.addClobberNode=function(node){
if(!dojo.render.html.ie){
return;
}
if(!node["__doClobber__"]){
node.__doClobber__=true;
dojo._ie_clobber.clobberNodes.push(node);
node.__clobberAttrs__=[];
}
};
this.addClobberNodeAttrs=function(node,_201){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_201.length;x++){
node.__clobberAttrs__.push(_201[x]);
}
};
this.removeListener=function(node,_204,fp,_206){
if(!_206){
var _206=false;
}
_204=_204.toLowerCase();
if((_204=="onkey")||(_204=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_206);
}
_204="onkeypress";
}
if(_204.substr(0,2)=="on"){
_204=_204.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_204,fp,_206);
}
};
this.addListener=function(node,_208,fp,_20a,_20b){
if(!node){
return;
}
if(!_20a){
var _20a=false;
}
_208=_208.toLowerCase();
if((_208=="onkey")||(_208=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_20a,_20b);
}
_208="onkeypress";
}
if(_208.substr(0,2)!="on"){
_208="on"+_208;
}
if(!_20b){
var _20c=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_20a){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_20c=fp;
}
if(node.addEventListener){
node.addEventListener(_208.substr(2),_20c,_20a);
return _20c;
}else{
if(typeof node[_208]=="function"){
var _20f=node[_208];
node[_208]=function(e){
_20f(e);
return _20c(e);
};
}else{
node[_208]=_20c;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_208]);
}
return _20c;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_212,_213){
if(typeof _212!="function"){
dojo.raise("listener not a function: "+_212);
}
dojo.event.browser.currentEvent.currentTarget=_213;
return _212.call(_213,dojo.event.browser.currentEvent);
};
this.stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this.preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_216){
if(!evt){
if(window["event"]){
evt=window.event;
}
}
if((evt["type"])&&(evt["type"].indexOf("key")==0)){
evt.keys=this.revKeys;
for(var key in this.keys){
evt[key]=this.keys[key];
}
if(evt["type"]=="keydown"&&dojo.render.html.ie){
switch(evt.keyCode){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_LEFT_WINDOW:
case evt.KEY_RIGHT_WINDOW:
case evt.KEY_SELECT:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
case evt.KEY_NUMPAD_0:
case evt.KEY_NUMPAD_1:
case evt.KEY_NUMPAD_2:
case evt.KEY_NUMPAD_3:
case evt.KEY_NUMPAD_4:
case evt.KEY_NUMPAD_5:
case evt.KEY_NUMPAD_6:
case evt.KEY_NUMPAD_7:
case evt.KEY_NUMPAD_8:
case evt.KEY_NUMPAD_9:
case evt.KEY_NUMPAD_PERIOD:
break;
case evt.KEY_NUMPAD_MULTIPLY:
case evt.KEY_NUMPAD_PLUS:
case evt.KEY_NUMPAD_ENTER:
case evt.KEY_NUMPAD_MINUS:
case evt.KEY_NUMPAD_DIVIDE:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
case evt.KEY_PAGE_UP:
case evt.KEY_PAGE_DOWN:
case evt.KEY_END:
case evt.KEY_HOME:
case evt.KEY_LEFT_ARROW:
case evt.KEY_UP_ARROW:
case evt.KEY_RIGHT_ARROW:
case evt.KEY_DOWN_ARROW:
case evt.KEY_INSERT:
case evt.KEY_DELETE:
case evt.KEY_F1:
case evt.KEY_F2:
case evt.KEY_F3:
case evt.KEY_F4:
case evt.KEY_F5:
case evt.KEY_F6:
case evt.KEY_F7:
case evt.KEY_F8:
case evt.KEY_F9:
case evt.KEY_F10:
case evt.KEY_F11:
case evt.KEY_F12:
case evt.KEY_F12:
case evt.KEY_F13:
case evt.KEY_F14:
case evt.KEY_F15:
case evt.KEY_CLEAR:
case evt.KEY_HELP:
evt.key=evt.keyCode;
break;
default:
if(evt.ctrlKey||evt.altKey){
var _218=evt.keyCode;
if(_218>=65&&_218<=90&&evt.shiftKey==false){
_218+=32;
}
if(_218>=1&&_218<=26&&evt.ctrlKey){
_218+=96;
}
evt.key=String.fromCharCode(_218);
}
}
}else{
if(evt["type"]=="keypress"){
if(dojo.render.html.opera){
if(evt.which==0){
evt.key=evt.keyCode;
}else{
if(evt.which>0){
switch(evt.which){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
evt.key=evt.which;
break;
default:
var _218=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_218+=32;
}
evt.key=String.fromCharCode(_218);
}
}
}
}else{
if(dojo.render.html.ie){
if(!evt.ctrlKey&&!evt.altKey&&evt.keyCode>=evt.KEY_SPACE){
evt.key=String.fromCharCode(evt.keyCode);
}
}else{
if(dojo.render.html.safari){
switch(evt.keyCode){
case 63232:
evt.key=evt.KEY_UP_ARROW;
break;
case 63233:
evt.key=evt.KEY_DOWN_ARROW;
break;
case 63234:
evt.key=evt.KEY_LEFT_ARROW;
break;
case 63235:
evt.key=evt.KEY_RIGHT_ARROW;
break;
default:
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}else{
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}
}
}
}
}
if(dojo.render.html.ie){
if(!evt.target){
evt.target=evt.srcElement;
}
if(!evt.currentTarget){
evt.currentTarget=(_216?_216:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _21a=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_21a.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_21a.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this.stopPropagation;
evt.preventDefault=this.preventDefault;
}
return evt;
};
this.stopEvent=function(ev){
if(window.event){
ev.returnValue=false;
ev.cancelBubble=true;
}else{
ev.preventDefault();
ev.stopPropagation();
}
};
};
dojo.provide("dojo.event.*");
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_222,_223){
var out="";
for(var i=0;i<_222;i++){
out+=str;
if(_223&&i<_222-1){
out+=_223;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string");
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_232,_233,_234){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_232){
this.mimetype=_232;
}
if(_233){
this.transport=_233;
}
if(arguments.length>=4){
this.changeUrl=_234;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,evt){
},error:function(type,_239){
},timeout:function(type){
},handle:function(){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_23b){
if(_23b["url"]){
_23b.url=_23b.url.toString();
}
if(_23b["formNode"]){
_23b.formNode=dojo.byId(_23b.formNode);
}
if(!_23b["method"]&&_23b["formNode"]&&_23b["formNode"].method){
_23b.method=_23b["formNode"].method;
}
if(!_23b["handle"]&&_23b["handler"]){
_23b.handle=_23b.handler;
}
if(!_23b["load"]&&_23b["loaded"]){
_23b.load=_23b.loaded;
}
if(!_23b["changeUrl"]&&_23b["changeURL"]){
_23b.changeUrl=_23b.changeURL;
}
_23b.encoding=dojo.lang.firstValued(_23b["encoding"],djConfig["bindEncoding"],"");
_23b.sendTransport=dojo.lang.firstValued(_23b["sendTransport"],djConfig["ioSendTransport"],false);
var _23c=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_23b[fn]&&_23c(_23b[fn])){
continue;
}
if(_23b["handle"]&&_23c(_23b["handle"])){
_23b[fn]=_23b.handle;
}
}
dojo.lang.mixin(this,_23b);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_243){
if(!(_243 instanceof dojo.io.Request)){
try{
_243=new dojo.io.Request(_243);
}
catch(e){
dojo.debug(e);
}
}
var _244="";
if(_243["transport"]){
_244=_243["transport"];
if(!this[_244]){
return _243;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_243))){
_244=tmp;
}
}
if(_244==""){
return _243;
}
}
this[_244].bind(_243);
_243.bindSuccess=true;
return _243;
};
dojo.io.queueBind=function(_247){
if(!(_247 instanceof dojo.io.Request)){
try{
_247=new dojo.io.Request(_247);
}
catch(e){
dojo.debug(e);
}
}
var _248=_247.load;
_247.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_248.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _24a=_247.error;
_247.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_24a.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_247);
dojo.io._dispatchNextQueueBind();
return _247;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_24d,last){
var enc=/utf/i.test(_24d||"")?encodeURIComponent:dojo.string.encodeAscii;
var _250=[];
var _251=new Object();
for(var name in map){
var _253=function(elt){
var val=enc(name)+"="+enc(elt);
_250[(last==name)?"push":"unshift"](val);
};
if(!_251[name]){
var _256=map[name];
if(dojo.lang.isArray(_256)){
dojo.lang.forEach(_256,_253);
}else{
_253(_256);
}
}
}
return _250.join("&");
};
dojo.io.setIFrameSrc=function(_257,src,_259){
try{
var r=dojo.render.html;
if(!_259){
if(r.safari){
_257.location=src;
}else{
frames[_257.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_257.contentWindow.document;
}else{
if(r.safari){
idoc=_257.document;
}else{
idoc=_257.contentWindow;
}
}
if(!idoc){
_257.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_25c){
this.pairs=[];
this.returnWrappers=_25c||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_25e,wrap,_260,_261){
var type=(_261)?"unshift":"push";
this.pairs[type]([name,_25e,wrap,_260]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_268,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _268.replace(/\%\{(\w+)\}/g,function(_26b,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _26e=str.split(" ");
for(var i=0;i<_26e.length;i++){
_26e[i]=_26e[i].charAt(0).toUpperCase()+_26e[i].substring(1);
}
return _26e.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _273=escape(str);
var _274,re=/%u([0-9A-F]{4})/i;
while((_274=_273.match(re))){
var num=Number("0x"+_274[1]);
var _276=escape("&#"+num+";");
ret+=_273.substring(0,_274.index)+_276;
_273=_273.substring(_274.index+_274[0].length);
}
ret+=_273.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_27b){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_27b){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_284){
if(_284){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_288,_289){
if(_289){
str=str.toLowerCase();
_288=_288.toLowerCase();
}
return str.indexOf(_288)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_28f){
if(_28f=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_28f=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_291){
var _292=[];
for(var i=0,prevcomma=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_291){
_292.push(str.substring(prevcomma,i));
prevcomma=i+1;
}
}
_292.push(str.substr(prevcomma));
return _292;
};
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_295,wrap,_297){
dojo.json.jsonRegistry.register(name,_295,wrap,_297);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _29a=typeof (o);
if(_29a=="undefined"){
return "undefined";
}else{
if((_29a=="number")||(_29a=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_29a=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _29c;
if(typeof (o.__json__)=="function"){
_29c=o.__json__();
if(o!==_29c){
return me(_29c);
}
}
if(typeof (o.json)=="function"){
_29c=o.json();
if(o!==_29c){
return me(_29c);
}
}
if(_29a!="function"&&typeof (o.length)=="number"){
var res=[];
for(var i=0;i<o.length;i++){
var val=me(o[i]);
if(typeof (val)!="string"){
val="undefined";
}
res.push(val);
}
return "["+res.join(",")+"]";
}
try{
window.o=o;
_29c=dojo.json.jsonRegistry.match(o);
return me(_29c);
}
catch(e){
}
if(_29a=="function"){
return null;
}
res=[];
for(var k in o){
var _2a1;
if(typeof (k)=="number"){
_2a1="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_2a1=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_2a1+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(E){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _2a3=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_2a3.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_2a5,_2a6){
var node=_2a5.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_2a6&&node&&node.tagName&&node.tagName.toLowerCase()!=_2a6.toLowerCase()){
node=dojo.dom.nextElement(node,_2a6);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_2a8,_2a9){
var node=_2a8.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_2a9&&node&&node.tagName&&node.tagName.toLowerCase()!=_2a9.toLowerCase()){
node=dojo.dom.prevElement(node,_2a9);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_2ac){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_2ac&&_2ac.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_2ac);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_2ae){
if(!node){
return null;
}
if(_2ae){
_2ae=_2ae.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_2ae&&_2ae.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_2ae);
}
return node;
};
dojo.dom.moveChildren=function(_2af,_2b0,trim){
var _2b2=0;
if(trim){
while(_2af.hasChildNodes()&&_2af.firstChild.nodeType==dojo.dom.TEXT_NODE){
_2af.removeChild(_2af.firstChild);
}
while(_2af.hasChildNodes()&&_2af.lastChild.nodeType==dojo.dom.TEXT_NODE){
_2af.removeChild(_2af.lastChild);
}
}
while(_2af.hasChildNodes()){
_2b0.appendChild(_2af.firstChild);
_2b2++;
}
return _2b2;
};
dojo.dom.copyChildren=function(_2b3,_2b4,trim){
var _2b6=_2b3.cloneNode(true);
return this.moveChildren(_2b6,_2b4,trim);
};
dojo.dom.removeChildren=function(node){
var _2b8=node.childNodes.length;
while(node.hasChildNodes()){
node.removeChild(node.firstChild);
}
return _2b8;
};
dojo.dom.replaceChildren=function(node,_2ba){
dojo.dom.removeChildren(node);
node.appendChild(_2ba);
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_2bd,_2be){
var _2bf=[];
var _2c0=(_2bd&&(_2bd instanceof Function||typeof _2bd=="function"));
while(node){
if(!_2c0||_2bd(node)){
_2bf.push(node);
}
if(_2be&&_2bf.length>0){
return _2bf[0];
}
node=node.parentNode;
}
if(_2be){
return null;
}
return _2bf;
};
dojo.dom.getAncestorsByTag=function(node,tag,_2c3){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_2c3);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_2c8,_2c9){
if(_2c9&&node){
node=node.parentNode;
}
while(node){
if(node==_2c8){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _2cc=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _2cd=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_2cd.length;i++){
try{
doc=new ActiveXObject(_2cd[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_2cc.implementation)&&(_2cc.implementation.createDocument)){
doc=_2cc.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_2d0){
if(!_2d0){
_2d0="text/xml";
}
if(!dj_undef("DOMParser")){
var _2d1=new DOMParser();
return _2d1.parseFromString(str,_2d0);
}else{
if(!dj_undef("ActiveXObject")){
var _2d2=dojo.dom.createDocument();
if(_2d2){
_2d2.async=false;
_2d2.loadXML(str);
return _2d2;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _2d3=dojo.doc();
if(_2d3.createElement){
var tmp=_2d3.createElement("xml");
tmp.innerHTML=str;
if(_2d3.implementation&&_2d3.implementation.createDocument){
var _2d5=_2d3.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_2d5.importNode(tmp.childNodes.item(i),true);
}
return _2d5;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_2d8){
if(_2d8.firstChild){
_2d8.insertBefore(node,_2d8.firstChild);
}else{
_2d8.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_2db){
if(_2db!=true&&(node===ref||node.nextSibling===ref)){
return false;
}
var _2dc=ref.parentNode;
_2dc.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_2df){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_2df!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_2df);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_2e3){
if((!node)||(!ref)||(!_2e3)){
return false;
}
switch(_2e3.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_2e5,_2e6){
var _2e7=_2e5.childNodes;
if(!_2e7.length){
_2e5.appendChild(node);
return true;
}
var _2e8=null;
for(var i=0;i<_2e7.length;i++){
var _2ea=_2e7.item(i)["getAttribute"]?parseInt(_2e7.item(i).getAttribute("dojoinsertionindex")):-1;
if(_2ea<_2e6){
_2e8=_2e7.item(i);
}
}
if(_2e8){
return dojo.dom.insertAfter(node,_2e8);
}else{
return dojo.dom.insertBefore(node,_2e7.item(0));
}
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _2ed=dojo.doc();
dojo.dom.replaceChildren(node,_2ed.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _2ee="";
if(node==null){
return _2ee;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_2ee+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_2ee+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _2ee;
}
};
dojo.dom.hasParent=function(node){
return node&&node.parentNode&&dojo.dom.isNode(node.parentNode);
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_2f4,_2f5,_2f6){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_2f4,_2f5,_2f6);
}else{
var _2f7=elem.ownerDocument;
var _2f8=_2f7.createNode(2,_2f5,_2f4);
_2f8.nodeValue=_2f6;
elem.setAttributeNode(_2f8);
}
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:window.location.href,initialHash:window.location.hash,moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _2fd=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_2ff){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_2fd.apply(this,[_2ff]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _300=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_302){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_300){
_300.apply(this,[_302]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_305){
if(!dojo.render.html.opera){
var _306=this._getUrlQuery(_305.href);
if(_306==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_306==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_306==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _307=this.historyStack.pop();
if(!_307){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_307);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _30e=url.split("?");
if(_30e.length<2){
return null;
}else{
return _30e[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
dojo.io.checkChildrenForFile=function(node){
var _311=false;
var _312=node.getElementsByTagName("input");
dojo.lang.forEach(_312,function(_313){
if(_311){
return;
}
if(_313.getAttribute("type")=="file"){
_311=true;
}
});
return _311;
};
dojo.io.formHasFile=function(_314){
return dojo.io.checkChildrenForFile(_314);
};
dojo.io.updateNode=function(node,_316){
node=dojo.byId(node);
var args=_316;
if(dojo.lang.isString(_316)){
args={url:_316};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
if(dojo["event"]){
try{
dojo.event.browser.clean(node.firstChild);
}
catch(e){
}
}
node.removeChild(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_31d,_31e,_31f){
if((!_31d)||(!_31d.tagName)||(!_31d.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_31f){
_31f=dojo.io.formFilter;
}
var enc=/utf/i.test(_31e||"")?encodeURIComponent:dojo.string.encodeAscii;
var _321=[];
for(var i=0;i<_31d.elements.length;i++){
var elm=_31d.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_31f(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_321.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_321.push(name+"="+enc(elm.value));
}
}else{
_321.push(name+"="+enc(elm.value));
}
}
}
var _327=_31d.getElementsByTagName("input");
for(var i=0;i<_327.length;i++){
var _328=_327[i];
if(_328.type.toLowerCase()=="image"&&_328.form==_31d&&_31f(_328)){
var name=enc(_328.name);
_321.push(name+"="+enc(_328.value));
_321.push(name+".x=0");
_321.push(name+".y=0");
}
}
return _321.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _32e=form.getElementsByTagName("input");
for(var i=0;i<_32e.length;i++){
var _32f=_32e[i];
if(_32f.type.toLowerCase()=="image"&&_32f.form==form){
this.connect(_32f,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
e.preventDefault();
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _336=false;
if(node.disabled||!node.name){
_336=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_336=node==this.clickedButton;
}else{
_336=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _336;
},connect:function(_337,_338,_339){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_337,_338,this,_339);
}else{
var fcn=dojo.lang.hitch(this,_339);
_337[_338]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _33c=this;
var _33d={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_33f,_340){
return url+"|"+_33f+"|"+_340.toLowerCase();
}
function addToCache(url,_342,_343,http){
_33d[getCacheKey(url,_342,_343)]=http;
}
function getFromCache(url,_346,_347){
return _33d[getCacheKey(url,_346,_347)];
}
this.clearCache=function(){
_33d={};
};
function doLoad(_348,http,url,_34b,_34c){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_348.method.toLowerCase()=="head"){
var _34e=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _34e;
};
var _34f=_34e.split(/[\r\n]+/g);
for(var i=0;i<_34f.length;i++){
var pair=_34f[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_348.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_348.mimetype=="text/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_348.mimetype=="application/xml")||(_348.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_34c){
addToCache(url,_34b,_348.method,http);
}
_348[(typeof _348.load=="function")?"load":"handle"]("load",ret,http,_348);
}else{
var _352=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_348[(typeof _348.error=="function")?"error":"handle"]("error",_352,http,_348);
}
}
function setHeaders(http,_354){
if(_354["headers"]){
for(var _355 in _354["headers"]){
if(_355.toLowerCase()=="content-type"&&!_354["contentType"]){
_354["contentType"]=_354["headers"][_355];
}else{
http.setRequestHeader(_355,_354["headers"][_355]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_33c._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _359=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_35a){
return _359&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json"],(_35a["mimetype"].toLowerCase()||""))&&!(_35a["formNode"]&&dojo.io.formHasFile(_35a["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_35b){
if(!_35b["url"]){
if(!_35b["formNode"]&&(_35b["backButton"]||_35b["back"]||_35b["changeUrl"]||_35b["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_35b);
return true;
}
}
var url=_35b.url;
var _35d="";
if(_35b["formNode"]){
var ta=_35b.formNode.getAttribute("action");
if((ta)&&(!_35b["url"])){
url=ta;
}
var tp=_35b.formNode.getAttribute("method");
if((tp)&&(!_35b["method"])){
_35b.method=tp;
}
_35d+=dojo.io.encodeForm(_35b.formNode,_35b.encoding,_35b["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_35b["file"]){
_35b.method="post";
}
if(!_35b["method"]){
_35b.method="get";
}
if(_35b.method.toLowerCase()=="get"){
_35b.multipart=false;
}else{
if(_35b["file"]){
_35b.multipart=true;
}else{
if(!_35b["multipart"]){
_35b.multipart=false;
}
}
}
if(_35b["backButton"]||_35b["back"]||_35b["changeUrl"]){
dojo.undo.browser.addToHistory(_35b);
}
var _360=_35b["content"]||{};
if(_35b.sendTransport){
_360["dojo.transport"]="xmlhttp";
}
do{
if(_35b.postContent){
_35d=_35b.postContent;
break;
}
if(_360){
_35d+=dojo.io.argsFromMap(_360,_35b.encoding);
}
if(_35b.method.toLowerCase()=="get"||!_35b.multipart){
break;
}
var t=[];
if(_35d.length){
var q=_35d.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_35b.file){
if(dojo.lang.isArray(_35b.file)){
for(var i=0;i<_35b.file.length;++i){
var o=_35b.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_35b.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_35d=t.join("\r\n");
}
}while(false);
var _366=_35b["sync"]?false:true;
var _367=_35b["preventCache"]||(this.preventCache==true&&_35b["preventCache"]!=false);
var _368=_35b["useCache"]==true||(this.useCache==true&&_35b["useCache"]!=false);
if(!_367&&_368){
var _369=getFromCache(url,_35d,_35b.method);
if(_369){
doLoad(_35b,_369,url,_35d,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_35b);
var _36b=false;
if(_366){
var _36c=this.inFlight.push({"req":_35b,"http":http,"url":url,"query":_35d,"useCache":_368,"startTime":_35b.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_33c._blockAsync=true;
}
if(_35b.method.toLowerCase()=="post"){
http.open("POST",url,_366);
setHeaders(http,_35b);
http.setRequestHeader("Content-Type",_35b.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_35b.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_35d);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_35b,{status:404},url,_35d,_368);
}
}else{
var _36d=url;
if(_35d!=""){
_36d+=(_36d.indexOf("?")>-1?"&":"?")+_35d;
}
if(_367){
_36d+=(dojo.string.endsWithAny(_36d,"?","&")?"":(_36d.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
http.open(_35b.method.toUpperCase(),_36d,_366);
setHeaders(http,_35b);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_35b,{status:404},url,_35d,_368);
}
}
if(!_366){
doLoad(_35b,http,url,_35d,_368);
_33c._blockAsync=false;
}
_35b.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_36f,uri){
var loc=dojo.hostenv.getModulePrefix(_36f);
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri()+loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _374=new dojo.uri.Uri(arguments[i].toString());
var _375=new dojo.uri.Uri(uri.toString());
if(_374.path==""&&_374.scheme==null&&_374.authority==null&&_374.query==null){
if(_374.fragment!=null){
_375.fragment=_374.fragment;
}
_374=_375;
}else{
if(_374.scheme==null){
_374.scheme=_375.scheme;
if(_374.authority==null){
_374.authority=_375.authority;
if(_374.path.charAt(0)!="/"){
var path=_375.path.substring(0,_375.path.lastIndexOf("/")+1)+_374.path;
var segs=path.split("/");
for(var j=0;j<segs.length;j++){
if(segs[j]=="."){
if(j==segs.length-1){
segs[j]="";
}else{
segs.splice(j,1);
j--;
}
}else{
if(j>0&&!(j==1&&segs[0]=="")&&segs[j]==".."&&segs[j-1]!=".."){
if(j==segs.length-1){
segs.splice(j,1);
segs[j-1]="";
}else{
segs.splice(j-1,2);
j-=2;
}
}
}
}
_374.path=segs.join("/");
}
}
}
}
uri="";
if(_374.scheme!=null){
uri+=_374.scheme+":";
}
if(_374.authority!=null){
uri+="//"+_374.authority;
}
uri+=_374.path;
if(_374.query!=null){
uri+="?"+_374.query;
}
if(_374.fragment!=null){
uri+="#"+_374.fragment;
}
}
this.uri=uri.toString();
var _379="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_379));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_379="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_379));
this.user=r[3]||null;
this.password=r[4]||null;
this.host=r[5];
this.port=r[7]||null;
}
this.toString=function(){
return this.uri;
};
};
};
dojo.provide("dojo.uri.*");
dojo.provide("dojo.io.IframeIO");
dojo.io.createIFrame=function(_37b,_37c,uri){
if(window[_37b]){
return window[_37b];
}
if(window.frames[_37b]){
return window.frames[_37b];
}
var r=dojo.render.html;
var _37f=null;
var turi=uri||dojo.uri.dojoUri("iframe_history.html?noInit=true");
var _381=((r.ie)&&(dojo.render.os.win))?"<iframe name=\""+_37b+"\" src=\""+turi+"\" onload=\""+_37c+"\">":"iframe";
_37f=document.createElement(_381);
with(_37f){
name=_37b;
setAttribute("name",_37b);
id=_37b;
}
dojo.body().appendChild(_37f);
window[_37b]=_37f;
with(_37f.style){
if(!r.safari){
position="absolute";
}
left=top="0px";
height=width="1px";
visibility="hidden";
}
if(!r.ie){
dojo.io.setIFrameSrc(_37f,turi,true);
_37f.onload=new Function(_37c);
}
return _37f;
};
dojo.io.IframeTransport=new function(){
var _382=this;
this.currentRequest=null;
this.requestQueue=[];
this.iframeName="dojoIoIframe";
this.fireNextRequest=function(){
if((this.currentRequest)||(this.requestQueue.length==0)){
return;
}
var cr=this.currentRequest=this.requestQueue.shift();
cr._contentToClean=[];
var fn=cr["formNode"];
var _385=cr["content"]||{};
if(cr.sendTransport){
_385["dojo.transport"]="iframe";
}
if(fn){
if(_385){
for(var x in _385){
if(!fn[x]){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_385[x]+"'>");
fn.appendChild(tn);
}else{
tn=document.createElement("input");
fn.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_385[x];
}
cr._contentToClean.push(x);
}else{
fn[x].value=_385[x];
}
}
}
if(cr["url"]){
cr._originalAction=fn.getAttribute("action");
fn.setAttribute("action",cr.url);
}
if(!fn.getAttribute("method")){
fn.setAttribute("method",(cr["method"])?cr["method"]:"post");
}
cr._originalTarget=fn.getAttribute("target");
fn.setAttribute("target",this.iframeName);
fn.target=this.iframeName;
fn.submit();
}else{
var _388=dojo.io.argsFromMap(this.currentRequest.content);
var _389=(cr.url.indexOf("?")>-1?"&":"?")+_388;
dojo.io.setIFrameSrc(this.iframe,_389,true);
}
};
this.canHandle=function(_38a){
return ((dojo.lang.inArray(["text/plain","text/html","text/javascript","text/json"],_38a["mimetype"]))&&((_38a["formNode"])&&(dojo.io.checkChildrenForFile(_38a["formNode"])))&&(dojo.lang.inArray(["post","get"],_38a["method"].toLowerCase()))&&(!((_38a["sync"])&&(_38a["sync"]==true))));
};
this.bind=function(_38b){
if(!this["iframe"]){
this.setUpIframe();
}
this.requestQueue.push(_38b);
this.fireNextRequest();
return;
};
this.setUpIframe=function(){
this.iframe=dojo.io.createIFrame(this.iframeName,"dojo.io.IframeTransport.iframeOnload();");
};
this.iframeOnload=function(){
if(!_382.currentRequest){
_382.fireNextRequest();
return;
}
var req=_382.currentRequest;
var _38d=req._contentToClean;
for(var i=0;i<_38d.length;i++){
var key=_38d[i];
if(dojo.render.html.safari){
var _390=req.formNode;
for(var j=0;j<_390.childNodes.length;j++){
var _392=_390.childNodes[j];
if(_392.name==key){
var _393=_392.parentNode;
_393.removeChild(_392);
break;
}
}
}else{
if(req.formNode){
var _394=req.formNode[key];
req.formNode.removeChild(_394);
req.formNode[key]=null;
}
}
}
if(req["_originalAction"]){
req.formNode.setAttribute("action",req._originalAction);
}
req.formNode.setAttribute("target",req._originalTarget);
req.formNode.target=req._originalTarget;
var _395=function(_396){
var doc=_396.contentDocument||((_396.contentWindow)&&(_396.contentWindow.document))||((_396.name)&&(document.frames[_396.name])&&(document.frames[_396.name].document))||null;
return doc;
};
var ifd=_395(_382.iframe);
var _399;
var _39a=false;
try{
var cmt=req.mimetype;
if((cmt=="text/javascript")||(cmt=="text/json")){
var js=ifd.getElementsByTagName("textarea")[0].value;
if(cmt=="text/json"){
js="("+js+")";
}
_399=dj_eval(js);
}else{
if(cmt=="text/html"){
_399=ifd;
}else{
_399=ifd.getElementsByTagName("textarea")[0].value;
}
}
_39a=true;
}
catch(e){
var _39d=new dojo.io.Error("IframeTransport Error");
if(dojo.lang.isFunction(req["error"])){
req.error("error",_39d,req);
}
}
try{
if(_39a&&dojo.lang.isFunction(req["load"])){
req.load("load",_399,req);
}
}
catch(e){
throw e;
}
finally{
_382.currentRequest=null;
_382.fireNextRequest();
}
};
dojo.io.transports.addTransport("IframeTransport");
};
dojo.provide("dojo.io.ScriptSrcIO");
dojo.io.ScriptSrcTransport=new function(){
this.preventCache=false;
this.maxUrlLength=1000;
this.inFlightTimer=null;
this.DsrStatusCodes={Continue:100,Ok:200,Error:500};
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setInterval("dojo.io.ScriptSrcTransport.watchInFlight();",100);
}
};
this.watchInFlight=function(){
var _39e=0;
var _39f=0;
for(var _3a0 in this._state){
_39e++;
var _3a1=this._state[_3a0];
if(_3a1.isDone){
_39f++;
delete this._state[_3a0];
}else{
if(!_3a1.isFinishing){
var _3a2=_3a1.kwArgs;
try{
if(_3a1.checkString&&eval("typeof("+_3a1.checkString+") != 'undefined'")){
_3a1.isFinishing=true;
this._finish(_3a1,"load");
_39f++;
delete this._state[_3a0];
}else{
if(_3a2.timeoutSeconds&&_3a2.timeout){
if(_3a1.startTime+(_3a2.timeoutSeconds*1000)<(new Date()).getTime()){
_3a1.isFinishing=true;
this._finish(_3a1,"timeout");
_39f++;
delete this._state[_3a0];
}
}else{
if(!_3a2.timeoutSeconds){
_39f++;
}
}
}
}
catch(e){
_3a1.isFinishing=true;
this._finish(_3a1,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_39f>=_39e){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_3a3){
return dojo.lang.inArray(["text/javascript","text/json"],(_3a3["mimetype"].toLowerCase()))&&(_3a3["method"].toLowerCase()=="get")&&!(_3a3["formNode"]&&dojo.io.formHasFile(_3a3["formNode"]))&&(!_3a3["sync"]||_3a3["sync"]==false)&&!_3a3["file"]&&!_3a3["multipart"];
};
this.removeScripts=function(){
var _3a4=document.getElementsByTagName("script");
for(var i=0;_3a4&&i<_3a4.length;i++){
var _3a6=_3a4[i];
if(_3a6.className=="ScriptSrcTransport"){
var _3a7=_3a6.parentNode;
_3a7.removeChild(_3a6);
i--;
}
}
};
this.bind=function(_3a8){
var url=_3a8.url;
var _3aa="";
if(_3a8["formNode"]){
var ta=_3a8.formNode.getAttribute("action");
if((ta)&&(!_3a8["url"])){
url=ta;
}
var tp=_3a8.formNode.getAttribute("method");
if((tp)&&(!_3a8["method"])){
_3a8.method=tp;
}
_3aa+=dojo.io.encodeForm(_3a8.formNode,_3a8.encoding,_3a8["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _3ad=url.split("?");
if(_3ad&&_3ad.length==2){
url=_3ad[0];
_3aa+=(_3aa?"&":"")+_3ad[1];
}
if(_3a8["backButton"]||_3a8["back"]||_3a8["changeUrl"]){
dojo.undo.browser.addToHistory(_3a8);
}
var id=_3a8["apiId"]?_3a8["apiId"]:"id"+this._counter++;
var _3af=_3a8["content"];
var _3b0=_3a8.jsonParamName;
if(_3a8.sendTransport||_3b0){
if(!_3af){
_3af={};
}
if(_3a8.sendTransport){
_3af["dojo.transport"]="scriptsrc";
}
if(_3b0){
_3af[_3b0]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_3a8.postContent){
_3aa=_3a8.postContent;
}else{
if(_3af){
_3aa+=((_3aa)?"&":"")+dojo.io.argsFromMap(_3af,_3a8.encoding,_3b0);
}
}
if(_3a8["apiId"]){
_3a8["useRequestId"]=true;
}
var _3b1={"id":id,"idParam":"_dsrid="+id,"url":url,"query":_3aa,"kwArgs":_3a8,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(_3b1,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_3af&&_3af[_3b0]){
_3b1.jsonp=_3af[_3b0];
_3b1.jsonpCall=function(data){
if(data["Error"]||data["error"]){
if(dojo["json"]&&dojo["json"]["serialize"]){
dojo.debug(dojo.json.serialize(data));
}
dojo.io.ScriptSrcTransport._finish(this,"error",data);
}else{
dojo.io.ScriptSrcTransport._finish(this,"load",data);
}
};
}
if(_3a8["useRequestId"]||_3a8["checkString"]||_3b1["jsonp"]){
this._state[id]=_3b1;
}
if(_3a8["checkString"]){
_3b1.checkString=_3a8["checkString"];
}
_3b1.constantParams=(_3a8["constantParams"]==null?"":_3a8["constantParams"]);
if(_3a8["preventCache"]||(this.preventCache==true&&_3a8["preventCache"]!=false)){
_3b1.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
_3b1.nocacheParam="";
}
var _3b3=_3b1.url.length+_3b1.query.length+_3b1.constantParams.length+_3b1.nocacheParam.length+this._extraPaddingLength;
if(_3a8["useRequestId"]){
_3b3+=_3b1.idParam.length;
}
if(!_3a8["checkString"]&&_3a8["useRequestId"]&&!_3b1["jsonp"]&&!_3a8["forceSingleRequest"]&&_3b3>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(_3b1,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(_3b1,1);
}
}else{
var _3b4=[_3b1.constantParams,_3b1.nocacheParam,_3b1.query];
if(_3a8["useRequestId"]&&!_3b1["jsonp"]){
_3b4.unshift(_3b1.idParam);
}
var _3b5=this._buildUrl(_3b1.url,_3b4);
_3b1.finalUrl=_3b5;
this._attach(_3b1.id,_3b5);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_3b7){
var _3b8=url;
var _3b9="?";
for(var i=0;i<_3b7.length;i++){
if(_3b7[i]){
_3b8+=_3b9+_3b7[i];
_3b9="&";
}
}
return _3b8;
};
this._attach=function(id,url){
var _3bd=document.createElement("script");
_3bd.type="text/javascript";
_3bd.src=url;
_3bd.id=id;
_3bd.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_3bd);
};
this._multiAttach=function(_3be,part){
if(_3be.query==null){
this._finish(_3be,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!_3be.constantParams){
_3be.constantParams="";
}
var _3c0=this.maxUrlLength-_3be.idParam.length-_3be.constantParams.length-_3be.url.length-_3be.nocacheParam.length-this._extraPaddingLength;
var _3c1=_3be.query.length<_3c0;
var _3c2;
if(_3c1){
_3c2=_3be.query;
_3be.query=null;
}else{
var _3c3=_3be.query.lastIndexOf("&",_3c0-1);
var _3c4=_3be.query.lastIndexOf("=",_3c0-1);
if(_3c3>_3c4||_3c4==_3c0-1){
_3c2=_3be.query.substring(0,_3c3);
_3be.query=_3be.query.substring(_3c3+1,_3be.query.length);
}else{
_3c2=_3be.query.substring(0,_3c0);
var _3c5=_3c2.substring((_3c3==-1?0:_3c3+1),_3c4);
_3be.query=_3c5+"="+_3be.query.substring(_3c0,_3be.query.length);
}
}
var _3c6=[_3c2,_3be.idParam,_3be.constantParams,_3be.nocacheParam];
if(!_3c1){
_3c6.push("_part="+part);
}
var url=this._buildUrl(_3be.url,_3c6);
this._attach(_3be.id+"_"+part,url);
};
this._finish=function(_3c8,_3c9,_3ca){
if(_3c9!="partOk"&&!_3c8.kwArgs[_3c9]&&!_3c8.kwArgs["handle"]){
if(_3c9=="error"){
_3c8.isDone=true;
throw _3ca;
}
}else{
switch(_3c9){
case "load":
var _3cb=_3ca?_3ca.response:null;
if(!_3cb){
_3cb=_3ca;
}
_3c8.kwArgs[(typeof _3c8.kwArgs.load=="function")?"load":"handle"]("load",_3cb,_3ca,_3c8.kwArgs);
_3c8.isDone=true;
break;
case "partOk":
var part=parseInt(_3ca.response.part,10)+1;
if(_3ca.response.constantParams){
_3c8.constantParams=_3ca.response.constantParams;
}
this._multiAttach(_3c8,part);
_3c8.isDone=false;
break;
case "error":
_3c8.kwArgs[(typeof _3c8.kwArgs.error=="function")?"error":"handle"]("error",_3ca.response,_3ca,_3c8.kwArgs);
_3c8.isDone=true;
break;
default:
_3c8.kwArgs[(typeof _3c8.kwArgs[_3c9]=="function")?_3c9:"handle"](_3c9,_3ca,_3ca,_3c8.kwArgs);
_3c8.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
window.onscriptload=function(_3cd){
var _3ce=null;
var _3cf=dojo.io.ScriptSrcTransport;
if(_3cf._state[_3cd.id]){
_3ce=_3cf._state[_3cd.id];
}else{
var _3d0;
for(var _3d1 in _3cf._state){
_3d0=_3cf._state[_3d1];
if(_3d0.finalUrl&&_3d0.finalUrl==_3cd.id){
_3ce=_3d0;
break;
}
}
if(_3ce==null){
var _3d2=document.getElementsByTagName("script");
for(var i=0;_3d2&&i<_3d2.length;i++){
var _3d4=_3d2[i];
if(_3d4.getAttribute("class")=="ScriptSrcTransport"&&_3d4.src==_3cd.id){
_3ce=_3cf._state[_3d4.id];
break;
}
}
}
if(_3ce==null){
throw "No matching state for onscriptload event.id: "+_3cd.id;
}
}
var _3d5="error";
switch(_3cd.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_3d5="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_3d5="load";
break;
}
_3cf._finish(_3ce,_3d5,_3cd);
};
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_3d7,days,path,_3da,_3db){
var _3dc=-1;
if(typeof days=="number"&&days>=0){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_3dc=d.toGMTString();
}
_3d7=escape(_3d7);
document.cookie=name+"="+_3d7+";"+(_3dc!=-1?" expires="+_3dc+";":"")+(path?"path="+path:"")+(_3da?"; domain="+_3da:"")+(_3db?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _3e0=document.cookie.substring(idx+name.length+1);
var end=_3e0.indexOf(";");
if(end==-1){
end=_3e0.length;
}
_3e0=_3e0.substring(0,end);
_3e0=unescape(_3e0);
return _3e0;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_3e7,_3e8,_3e9){
if(arguments.length==5){
_3e9=_3e7;
_3e7=null;
_3e8=null;
}
var _3ea=[],cookie,value="";
if(!_3e9){
cookie=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!cookie){
cookie={};
}
for(var prop in obj){
if(prop==null){
delete cookie[prop];
}else{
if(typeof obj[prop]=="string"||typeof obj[prop]=="number"){
cookie[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in cookie){
_3ea.push(escape(prop)+"="+escape(cookie[prop]));
}
value=_3ea.join("&");
}
dojo.io.cookie.setCookie(name,value,days,path,_3e7,_3e8);
};
dojo.io.cookie.getObjectCookie=function(name){
var _3ed=null,cookie=dojo.io.cookie.getCookie(name);
if(cookie){
_3ed={};
var _3ee=cookie.split("&");
for(var i=0;i<_3ee.length;i++){
var pair=_3ee[i].split("=");
var _3f1=pair[1];
if(isNaN(_3f1)){
_3f1=unescape(pair[1]);
}
_3ed[unescape(pair[0])]=_3f1;
}
}
return _3ed;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _3f2=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_3f2=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.io.cometd");
dojo.provide("cometd");
cometd=new function(){
this.initialized=false;
this.connected=false;
this.connectionTypes=new dojo.AdapterRegistry(true);
this.version=0.1;
this.minimumVersion=0.1;
this.clientId=null;
this.isXD=false;
this.handshakeReturn=null;
this.currentTransport=null;
this.url=null;
this.lastMessage=null;
this.globalTopicChannels={};
this.backlog=[];
this.tunnelInit=function(_3f3,_3f4){
};
this.tunnelCollapse=function(){
dojo.debug("tunnel collapsed!");
};
this.init=function(_3f5,root,_3f7){
_3f5=_3f5||{};
_3f5.version=this.version;
_3f5.minimumVersion=this.minimumVersion;
_3f5.channel="/meta/handshake";
this.url=root||djConfig["cometdRoot"];
if(!this.url){
dojo.debug("no cometd root specified in djConfig and no root passed");
return;
}
var _3f8={url:this.url,method:"POST",mimetype:"text/json",load:dojo.lang.hitch(this,"finishInit"),content:{"message":dojo.json.serialize([_3f5])}};
var _3f9="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=(""+window.location).match(new RegExp(_3f9));
if(r[4]){
var tmp=r[4].split(":");
var _3fc=tmp[0];
var _3fd=tmp[1]||"80";
r=this.url.match(new RegExp(_3f9));
if(r[4]){
tmp=r[4].split(":");
var _3fe=tmp[0];
var _3ff=tmp[1]||"80";
if((_3fe!=_3fc)||(_3ff!=_3fd)){
dojo.debug(_3fc,_3fe);
dojo.debug(_3fd,_3ff);
this.isXD=true;
_3f8.transport="ScriptSrcTransport";
_3f8.jsonParamName="jsonp";
}
}
}
if(_3f7){
dojo.lang.mixin(_3f8,_3f7);
}
return dojo.io.bind(_3f8);
};
this.finishInit=function(type,data,evt,_403){
this.handshakeReturn=data;
if(data["authSuccessful"]==false){
dojo.debug("cometd authentication failed");
return;
}
if(data.version<this.minimumVersion){
dojo.debug("cometd protocol version mismatch. We wanted",this.minimumVersion,"but got",data.version);
return;
}
this.currentTransport=this.connectionTypes.match(data.supportedConnectionTypes,data.version,this.isXD);
this.currentTransport.version=data.version;
this.clientId=data.clientId;
this.tunnelInit=dojo.lang.hitch(this.currentTransport,"tunnelInit");
this.tunnelCollapse=dojo.lang.hitch(this.currentTransport,"tunnelCollapse");
this.initialized=true;
this.currentTransport.startup(data);
while(this.backlog.length!=0){
var cur=this.backlog.shift();
var fn=cur.shift();
this[fn].apply(this,cur);
}
};
this.getRandStr=function(){
return Math.random().toString().substring(2,10);
};
this.deliver=function(_406){
dojo.lang.forEach(_406,this._deliver,this);
};
this._deliver=function(_407){
if(!_407["channel"]){
dojo.debug("cometd error: no channel for message!");
return;
}
if(!this.currentTransport){
this.backlog.push(["deliver",_407]);
return;
}
this.lastMessage=_407;
if((_407.channel.length>5)&&(_407.channel.substr(0,5)=="/meta")){
switch(_407.channel){
case "/meta/subscribe":
if(!_407.successful){
dojo.debug("cometd subscription error for channel",_407.channel,":",_407.error);
return;
}
this.subscribed(_407.subscription,_407);
break;
case "/meta/unsubscribe":
if(!_407.successful){
dojo.debug("cometd unsubscription error for channel",_407.channel,":",_407.error);
return;
}
this.unsubscribed(_407.subscription,_407);
break;
}
}
this.currentTransport.deliver(_407);
var _408=(this.globalTopicChannels[_407.channel])?_407.channel:"/cometd"+_407.channel;
dojo.event.topic.publish(_408,_407);
};
this.disconnect=function(){
if(!this.currentTransport){
dojo.debug("no current transport to disconnect from");
return;
}
this.currentTransport.disconnect();
};
this.publish=function(_409,data,_40b){
if(!this.currentTransport){
this.backlog.push(["publish",_409,data,_40b]);
return;
}
var _40c={data:data,channel:_409};
if(_40b){
dojo.lang.mixin(_40c,_40b);
}
return this.currentTransport.sendMessage(_40c);
};
this.subscribe=function(_40d,_40e,_40f,_410){
if(!this.currentTransport){
this.backlog.push(["subscribe",_40d,_40e,_40f,_410]);
return;
}
if(_40f){
var _411=(_40e)?_40d:"/cometd"+_40d;
if(_40e){
this.globalTopicChannels[_40d]=true;
}
dojo.event.topic.subscribe(_411,_40f,_410);
}
return this.currentTransport.sendMessage({channel:"/meta/subscribe",subscription:_40d});
};
this.subscribed=function(_412,_413){
dojo.debug(_412);
dojo.debugShallow(_413);
};
this.unsubscribe=function(_414,_415,_416,_417){
if(!this.currentTransport){
this.backlog.push(["unsubscribe",_414,_415,_416,_417]);
return;
}
if(_416){
var _418=(_415)?_414:"/cometd"+_414;
dojo.event.topic.unsubscribe(_418,_416,_417);
}
return this.currentTransport.sendMessage({channel:"/meta/unsubscribe",subscription:_414});
};
this.unsubscribed=function(_419,_41a){
dojo.debug(_419);
dojo.debugShallow(_41a);
};
};
cometd.iframeTransport=new function(){
this.connected=false;
this.connectionId=null;
this.rcvNode=null;
this.rcvNodeName="";
this.phonyForm=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_41b,_41c,_41d){
return ((!_41d)&&(!dojo.render.html.safari)&&(dojo.lang.inArray(_41b,"iframe")));
};
this.tunnelInit=function(){
this.postToIframe({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"iframe"}])});
};
this.tunnelCollapse=function(){
if(this.connected){
this.connected=false;
this.postToIframe({message:dojo.json.serialize([{channel:"/meta/reconnect",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=function(_41e){
if(_41e["timestamp"]){
this.lastTimestamp=_41e.timestamp;
}
if(_41e["id"]){
this.lastId=_41e.id;
}
if((_41e.channel.length>5)&&(_41e.channel.substr(0,5)=="/meta")){
switch(_41e.channel){
case "/meta/connect":
if(!_41e.successful){
dojo.debug("cometd connection error:",_41e.error);
return;
}
this.connectionId=_41e.connectionId;
this.connected=true;
this.processBacklog();
break;
case "/meta/reconnect":
if(!_41e.successful){
dojo.debug("cometd reconnection error:",_41e.error);
return;
}
this.connected=true;
break;
case "/meta/subscribe":
if(!_41e.successful){
dojo.debug("cometd subscription error for channel",_41e.channel,":",_41e.error);
return;
}
dojo.debug(_41e.channel);
break;
}
}
};
this.widenDomain=function(_41f){
var cd=_41f||document.domain;
if(cd.indexOf(".")==-1){
return;
}
var dps=cd.split(".");
if(dps.length<=2){
return;
}
dps=dps.slice(dps.length-2);
document.domain=dps.join(".");
return document.domain;
};
this.postToIframe=function(_422,url){
if(!this.phonyForm){
if(dojo.render.html.ie){
this.phonyForm=document.createElement("<form enctype='application/x-www-form-urlencoded' method='POST' style='display: none;'>");
dojo.body().appendChild(this.phonyForm);
}else{
this.phonyForm=document.createElement("form");
this.phonyForm.style.display="none";
dojo.body().appendChild(this.phonyForm);
this.phonyForm.enctype="application/x-www-form-urlencoded";
this.phonyForm.method="POST";
}
}
this.phonyForm.action=url||cometd.url;
this.phonyForm.target=this.rcvNodeName;
this.phonyForm.setAttribute("target",this.rcvNodeName);
while(this.phonyForm.firstChild){
this.phonyForm.removeChild(this.phonyForm.firstChild);
}
for(var x in _422){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_422[x]+"'>");
this.phonyForm.appendChild(tn);
}else{
tn=document.createElement("input");
this.phonyForm.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_422[x];
}
}
this.phonyForm.submit();
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_426,_427){
if((_427)||(this.connected)){
_426.connectionId=this.connectionId;
_426.clientId=cometd.clientId;
var _428={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_426])}};
return dojo.io.bind(_428);
}else{
this.backlog.push(_426);
}
};
this.startup=function(_429){
dojo.debug("startup!");
dojo.debug(dojo.json.serialize(_429));
if(this.connected){
return;
}
this.rcvNodeName="cometdRcv_"+cometd.getRandStr();
var _42a=cometd.url+"/?tunnelInit=iframe";
if(false&&dojo.render.html.ie){
this.rcvNode=new ActiveXObject("htmlfile");
this.rcvNode.open();
this.rcvNode.write("<html>");
this.rcvNode.write("<script>document.domain = '"+document.domain+"'");
this.rcvNode.write("</html>");
this.rcvNode.close();
var _42b=this.rcvNode.createElement("div");
this.rcvNode.appendChild(_42b);
this.rcvNode.parentWindow.dojo=dojo;
_42b.innerHTML="<iframe src='"+_42a+"'></iframe>";
}else{
this.rcvNode=dojo.io.createIFrame(this.rcvNodeName,"",_42a);
}
};
};
cometd.mimeReplaceTransport=new function(){
this.connected=false;
this.connectionId=null;
this.xhr=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_42c,_42d,_42e){
return ((!_42e)&&(dojo.render.html.mozilla)&&(dojo.lang.inArray(_42c,"mime-message-block")));
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"mime-message-block"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(this.connected){
this.connected=false;
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.handleOnLoad=function(resp){
cometd.deliver(dojo.json.evalJson(this.xhr.responseText));
};
this.openTunnelWith=function(_430,url){
this.xhr=dojo.hostenv.getXmlhttpObject();
this.xhr.multipart=true;
if(dojo.render.html.mozilla){
this.xhr.addEventListener("load",dojo.lang.hitch(this,"handleOnLoad"),false);
}else{
if(dojo.render.html.safari){
dojo.debug("Webkit is broken with multipart responses over XHR = (");
this.xhr.onreadystatechange=dojo.lang.hitch(this,"handleOnLoad");
}else{
this.xhr.onload=dojo.lang.hitch(this,"handleOnLoad");
}
}
this.xhr.open("POST",(url||cometd.url),true);
this.xhr.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
dojo.debug(dojo.json.serialize(_430));
this.xhr.send(dojo.io.argsFromMap(_430,"utf8"));
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_432,_433){
if((_433)||(this.connected)){
_432.connectionId=this.connectionId;
_432.clientId=cometd.clientId;
var _434={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_432])}};
return dojo.io.bind(_434);
}else{
this.backlog.push(_432);
}
};
this.startup=function(_435){
dojo.debugShallow(_435);
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.longPollTransport=new function(){
this.connected=false;
this.connectionId=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_436,_437,_438){
return ((!_438)&&(dojo.lang.inArray(_436,"long-polling")));
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"long-polling"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(!this.connected){
this.connected=false;
dojo.debug("clientId:",cometd.clientId);
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",connectionType:"long-polling",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.openTunnelWith=function(_439,url){
dojo.io.bind({url:(url||cometd.url),method:"post",content:_439,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,data,evt,args){
cometd.deliver(data);
this.connected=false;
this.tunnelCollapse();
}),error:function(){
dojo.debug("tunnel opening failed");
}});
this.connected=true;
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_43f,_440){
if((_440)||(this.connected)){
_43f.connectionId=this.connectionId;
_43f.clientId=cometd.clientId;
var _441={url:cometd.url||djConfig["cometdRoot"],method:"post",mimetype:"text/json",content:{message:dojo.json.serialize([_43f])}};
return dojo.io.bind(_441);
}else{
this.backlog.push(_43f);
}
};
this.startup=function(_442){
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.callbackPollTransport=new function(){
this.connected=false;
this.connectionId=null;
this.authToken=null;
this.lastTimestamp=null;
this.lastId=null;
this.backlog=[];
this.check=function(_443,_444,_445){
return dojo.lang.inArray(_443,"callback-polling");
};
this.tunnelInit=function(){
if(this.connected){
return;
}
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/connect",clientId:cometd.clientId,connectionType:"callback-polling"}])});
this.connected=true;
};
this.tunnelCollapse=function(){
if(!this.connected){
this.connected=false;
this.openTunnelWith({message:dojo.json.serialize([{channel:"/meta/reconnect",connectionType:"long-polling",clientId:cometd.clientId,connectionId:this.connectionId,timestamp:this.lastTimestamp,id:this.lastId}])});
}
};
this.deliver=cometd.iframeTransport.deliver;
this.openTunnelWith=function(_446,url){
dojo.io.bind({url:(url||cometd.url),content:_446,transport:"ScriptSrcTransport",jsonParamName:"jsonp",load:dojo.lang.hitch(this,function(type,data,evt,args){
dojo.debug(dojo.json.serialize(data));
cometd.deliver(data);
this.connected=false;
this.tunnelCollapse();
}),error:function(){
dojo.debug("tunnel opening failed");
}});
this.connected=true;
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_44c,_44d){
if((_44d)||(this.connected)){
_44c.connectionId=this.connectionId;
_44c.clientId=cometd.clientId;
var _44e={url:cometd.url||djConfig["cometdRoot"],transport:"ScriptSrcTransport",jsonParamName:"jsonp",content:{message:dojo.json.serialize([_44c])}};
return dojo.io.bind(_44e);
}else{
this.backlog.push(_44c);
}
};
this.startup=function(_44f){
if(this.connected){
return;
}
this.tunnelInit();
};
};
cometd.connectionTypes.register("mime-message-block",cometd.mimeReplaceTransport.check,cometd.mimeReplaceTransport);
cometd.connectionTypes.register("long-polling",cometd.longPollTransport.check,cometd.longPollTransport);
cometd.connectionTypes.register("callback-polling",cometd.callbackPollTransport.check,cometd.callbackPollTransport);
cometd.connectionTypes.register("iframe",cometd.iframeTransport.check,cometd.iframeTransport);
dojo.io.cometd=cometd;

