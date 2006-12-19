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
dojo.version={major:0,minor:0,patch:0,flag:"dev",revision:Number("$Rev: 6912 $".match(/[0-9]+/)[0]),toString:function(){
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
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}else{
_12=dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
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
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
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
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},registerModulePath:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
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
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
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
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
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
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.registerModulePath(_66,_67);
};
dojo.exists=function(obj,_69){
var p=_69.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6c){
var _6d=_6c?_6c.toLowerCase():dojo.locale;
if(_6d=="root"){
_6d="ROOT";
}
return _6d;
};
dojo.hostenv.searchLocalePath=function(_6e,_6f,_70){
_6e=dojo.hostenv.normalizeLocale(_6e);
var _71=_6e.split("-");
var _72=[];
for(var i=_71.length;i>0;i--){
_72.push(_71.slice(0,i).join("-"));
}
_72.push(false);
if(_6f){
_72.reverse();
}
for(var j=_72.length-1;j>=0;j--){
var loc=_72[j]||"ROOT";
var _76=_70(loc);
if(_76){
break;
}
}
};
dojo.hostenv.localesGenerated;
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_77){
_77=dojo.hostenv.normalizeLocale(_77);
dojo.hostenv.searchLocalePath(_77,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7a=djConfig.extraLocale||[];
for(var i=0;i<_7a.length;i++){
preload(_7a[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7c,_7d,_7e,_7f){
dojo.hostenv.preloadLocalizations();
var _80=dojo.hostenv.normalizeLocale(_7e);
var _81=[_7c,"nls",_7d].join(".");
var _82="";
if(_7f){
var _83=_7f.split(",");
for(var i=0;i<_83.length;i++){
if(_80.indexOf(_83[i])==0){
if(_83[i].length>_82.length){
_82=_83[i];
}
}
}
if(!_82){
_82="ROOT";
}
}
var _85=_7f?_82:_80;
var _86=dojo.hostenv.findModule(_81);
var _87=null;
if(_86){
if(djConfig.localizationComplete&&_86._built){
return;
}
var _88=_85.replace("-","_");
var _89=_81+"."+_88;
_87=dojo.hostenv.findModule(_89);
}
if(!_87){
_86=dojo.hostenv.startPackage(_81);
var _8a=dojo.hostenv.getModuleSymbols(_7c);
var _8b=_8a.concat("nls").join("/");
var _8c;
dojo.hostenv.searchLocalePath(_85,_7f,function(loc){
var _8e=loc.replace("-","_");
var _8f=_81+"."+_8e;
var _90=false;
if(!dojo.hostenv.findModule(_8f)){
dojo.hostenv.startPackage(_8f);
var _91=[_8b];
if(loc!="ROOT"){
_91.push(loc);
}
_91.push(_7d);
var _92=_91.join("/")+".js";
_90=dojo.hostenv.loadPath(_92,null,function(_93){
var _94=function(){
};
_94.prototype=_8c;
_86[_8e]=new _94();
for(var j in _93){
_86[_8e][j]=_93[j];
}
});
}else{
_90=true;
}
if(_90&&_86[_8e]){
_8c=_86[_8e];
}else{
_86[_8e]=_8c;
}
if(_7f){
return true;
}
});
}
if(_7f&&_80!=_82){
_86[_80.replace("-","_")]=_86[_82.replace("-","_")];
}
};
(function(){
var _96=djConfig.extraLocale;
if(_96){
if(!_96 instanceof Array){
_96=[_96];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_9a,_9b){
req(m,b,_9a,_9b);
if(_9a){
return;
}
for(var i=0;i<_96.length;i++){
req(m,b,_96[i],_9b);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _9d=document.location.toString();
var _9e=_9d.split("?",2);
if(_9e.length>1){
var _9f=_9e[1];
var _a0=_9f.split("&");
for(var x in _a0){
var sp=_a0[x].split("=");
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
var _a4=document.getElementsByTagName("script");
var _a5=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_a4.length;i++){
var src=_a4[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_a5);
if(m){
var _a9=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_a9+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_a9;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_a9;
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
var _b1=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_b1>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_b1+6,_b1+14);
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
var _b3=window["document"];
var tdi=_b3["implementation"];
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
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b7=null;
var _b8=null;
try{
_b7=new XMLHttpRequest();
}
catch(e){
}
if(!_b7){
for(var i=0;i<3;++i){
var _ba=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b7=new ActiveXObject(_ba);
}
catch(e){
_b8=e;
}
if(_b7){
dojo.hostenv._XMLHTTP_PROGIDS=[_ba];
break;
}
}
}
if(!_b7){
return dojo.raise("XMLHTTP not available",_b8);
}
return _b7;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_bc,_bd){
if(!_bc){
this._blockAsync=true;
}
var _be=this.getXmlhttpObject();
function isDocumentOk(_bf){
var _c0=_bf["status"];
return Boolean((!_c0)||((200<=_c0)&&(300>_c0))||(_c0==304));
}
if(_bc){
var _c1=this,_c2=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_be.onreadystatechange=function(){
if(_c2){
gbl.clearTimeout(_c2);
_c2=null;
}
if(_c1._blockAsync||(xhr&&xhr._blockAsync)){
_c2=gbl.setTimeout(function(){
_be.onreadystatechange.apply(this);
},10);
}else{
if(4==_be.readyState){
if(isDocumentOk(_be)){
_bc(_be.responseText);
}
}
}
};
}
_be.open("GET",uri,_bc?true:false);
try{
_be.send(null);
if(_bc){
return null;
}
if(!isDocumentOk(_be)){
var err=Error("Unable to load "+uri+" status:"+_be.status);
err.status=_be.status;
err.responseText=_be.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_bd)&&(!_bc)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _be.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_c6){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_c6);
}else{
try{
var _c7=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c7){
_c7=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_c6));
_c7.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_c6+"</div>");
}
catch(e2){
window.status=_c6;
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
function dj_addNodeEvtHdlr(_c9,_ca,fp){
var _cc=_c9["on"+_ca]||function(){
};
_c9["on"+_ca]=function(){
fp.apply(_c9,arguments);
_cc.apply(_c9,arguments);
};
return true;
}
function dj_load_init(e){
var _ce=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_ce!="domcontentloaded"&&_ce!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _cf=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_cf();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_cf);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.write("<scr"+"ipt defer src=\"//:\" "+"onreadystatechange=\"if(this.readyState=='complete'){dj_load_init();}\">"+"</scr"+"ipt>");
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _d0=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_d0=_d0.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_d0=_d0.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_d0.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _d1=new dojo.xml.Parse();
if(_d0.length>0){
for(var x=0;x<_d0.length;x++){
var _d3=document.getElementById(_d0[x]);
if(!_d3){
continue;
}
var _d4=_d1.parseElement(_d3,null,true);
dojo.widget.getParser().createComponents(_d4);
}
}else{
if(djConfig.parseWidgets){
var _d4=_d1.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_d4);
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
dojo.setContext=function(_d9,_da){
dj_currentContext=_d9;
dj_currentDocument=_da;
};
dojo._fireCallback=function(_db,_dc,_dd){
if((_dc)&&((typeof _db=="string")||(_db instanceof String))){
_db=_dc[_db];
}
return (_dc?_db.apply(_dc,_dd||[]):_db());
};
dojo.withGlobal=function(_de,_df,_e0,_e1){
var _e2;
var _e3=dj_currentContext;
var _e4=dj_currentDocument;
try{
dojo.setContext(_de,_de.document);
_e2=dojo._fireCallback(_df,_e0,_e1);
}
finally{
dojo.setContext(_e3,_e4);
}
return _e2;
};
dojo.withDoc=function(_e5,_e6,_e7,_e8){
var _e9;
var _ea=dj_currentDocument;
try{
dj_currentDocument=_e5;
_e9=dojo._fireCallback(_e6,_e7,_e8);
}
finally{
dj_currentDocument=_ea;
}
return _e9;
};
}
(function(){
if(typeof dj_usingBootstrap!="undefined"){
return;
}
var _eb=false;
var _ec=false;
var _ed=false;
if((typeof this["load"]=="function")&&((typeof this["Packages"]=="function")||(typeof this["Packages"]=="object"))){
_eb=true;
}else{
if(typeof this["load"]=="function"){
_ec=true;
}else{
if(window.widget){
_ed=true;
}
}
}
var _ee=[];
if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
_ee.push("debug.js");
}
if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!_eb)&&(!_ed)){
_ee.push("browser_debug.js");
}
var _ef=djConfig["baseScriptUri"];
if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
_ef=djConfig["baseLoaderUri"];
}
for(var x=0;x<_ee.length;x++){
var _f1=_ef+"src/"+_ee[x];
if(_eb||_ec){
load(_f1);
}else{
try{
document.write("<scr"+"ipt type='text/javascript' src='"+_f1+"'></scr"+"ipt>");
}
catch(e){
var _f2=document.createElement("script");
_f2.src=_f1;
document.getElementsByTagName("head")[0].appendChild(_f2);
}
}
}
})();
if(!this["dojo"]){
alert("\"dojo/__package__.js\" is now located at \"dojo/dojo.js\". Please update your includes accordingly");
}
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_f3,_f4){
if(!dojo.lang.isFunction(_f4)){
dojo.raise("dojo.inherits: superclass argument ["+_f4+"] must be a function (subclass: ["+_f3+"']");
}
_f3.prototype=new _f4();
_f3.prototype.constructor=_f3;
_f3.superclass=_f4.prototype;
_f3["super"]=_f4.prototype;
};
dojo.lang._mixin=function(obj,_f6){
var _f7={};
for(var x in _f6){
if((typeof _f7[x]=="undefined")||(_f7[x]!=_f6[x])){
obj[x]=_f6[x];
}
}
if(dojo.render.html.ie&&(typeof (_f6["toString"])=="function")&&(_f6["toString"]!=obj["toString"])&&(_f6["toString"]!=_f7["toString"])){
obj.toString=_f6.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_fa){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_fd,_fe){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_fd.prototype,arguments[i]);
}
return _fd;
};
dojo.lang._delegate=function(obj,_102){
function TMP(){
}
TMP.prototype=obj;
var tmp=new TMP();
if(_102){
dojo.lang.mixin(tmp,_102);
}
return tmp;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_104,_105,_106,_107){
var _108=dojo.lang.isString(_104);
if(_108){
_104=_104.split("");
}
if(_107){
var step=-1;
var i=_104.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_104.length;
}
if(_106){
while(i!=end){
if(_104[i]===_105){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_104[i]==_105){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_10c,_10d,_10e){
return dojo.lang.find(_10c,_10d,_10e,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_10f,_110){
return dojo.lang.find(_10f,_110)>-1;
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
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
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
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _11c=dojo.doc();
do{
var id="dj_unique_"+(++arguments.callee._idIncrement);
}while(_11c.getElementById(id));
return id;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_11e,_11f){
var node=_11e.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_11f&&node&&node.tagName&&node.tagName.toLowerCase()!=_11f.toLowerCase()){
node=dojo.dom.nextElement(node,_11f);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_121,_122){
var node=_121.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_122&&node&&node.tagName&&node.tagName.toLowerCase()!=_122.toLowerCase()){
node=dojo.dom.prevElement(node,_122);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_125){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_125&&_125.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_125);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_127){
if(!node){
return null;
}
if(_127){
_127=_127.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_127&&_127.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_127);
}
return node;
};
dojo.dom.moveChildren=function(_128,_129,trim){
var _12b=0;
if(trim){
while(_128.hasChildNodes()&&_128.firstChild.nodeType==dojo.dom.TEXT_NODE){
_128.removeChild(_128.firstChild);
}
while(_128.hasChildNodes()&&_128.lastChild.nodeType==dojo.dom.TEXT_NODE){
_128.removeChild(_128.lastChild);
}
}
while(_128.hasChildNodes()){
_129.appendChild(_128.firstChild);
_12b++;
}
return _12b;
};
dojo.dom.copyChildren=function(_12c,_12d,trim){
var _12f=_12c.cloneNode(true);
return this.moveChildren(_12f,_12d,trim);
};
dojo.dom.replaceChildren=function(node,_131){
var _132=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_132.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_131);
for(var i=0;i<_132.length;i++){
dojo.dom.destroyNode(_132[i]);
}
};
dojo.dom.removeChildren=function(node){
var _135=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _135;
};
dojo.dom.replaceNode=function(node,_137){
return node.parentNode.replaceChild(_137,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.evalObjPath("dojo.event.browser.clean",false)){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_13b,_13c){
var _13d=[];
var _13e=(_13b&&(_13b instanceof Function||typeof _13b=="function"));
while(node){
if(!_13e||_13b(node)){
_13d.push(node);
}
if(_13c&&_13d.length>0){
return _13d[0];
}
node=node.parentNode;
}
if(_13c){
return null;
}
return _13d;
};
dojo.dom.getAncestorsByTag=function(node,tag,_141){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_141);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_146,_147){
if(_147&&node){
node=node.parentNode;
}
while(node){
if(node==_146){
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
var _14a=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _14b=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_14b.length;i++){
try{
doc=new ActiveXObject(_14b[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_14a.implementation)&&(_14a.implementation.createDocument)){
doc=_14a.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_14e){
if(!_14e){
_14e="text/xml";
}
if(!dj_undef("DOMParser")){
var _14f=new DOMParser();
return _14f.parseFromString(str,_14e);
}else{
if(!dj_undef("ActiveXObject")){
var _150=dojo.dom.createDocument();
if(_150){
_150.async=false;
_150.loadXML(str);
return _150;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _151=dojo.doc();
if(_151.createElement){
var tmp=_151.createElement("xml");
tmp.innerHTML=str;
if(_151.implementation&&_151.implementation.createDocument){
var _153=_151.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_153.importNode(tmp.childNodes.item(i),true);
}
return _153;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_156){
if(_156.firstChild){
_156.insertBefore(node,_156.firstChild);
}else{
_156.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_159){
if((_159!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _15a=ref.parentNode;
_15a.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_15d){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_15d!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_15d);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_161){
if((!node)||(!ref)||(!_161)){
return false;
}
switch(_161.toLowerCase()){
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
dojo.dom.insertAtIndex=function(node,_163,_164){
var _165=_163.childNodes;
if(!_165.length||_165.length==_164){
_163.appendChild(node);
return true;
}
if(_164==0){
return dojo.dom.prependChild(node,_163);
}
return dojo.dom.insertAfter(node,_165[_164-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _168=dojo.doc();
dojo.dom.replaceChildren(node,_168.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _169="";
if(node==null){
return _169;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_169+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_169+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _169;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
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
dojo.dom.setAttributeNS=function(elem,_16f,_170,_171){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_16f,_170,_171);
}else{
var _172=elem.ownerDocument;
var _173=_172.createNode(2,_170,_16f);
_173.nodeValue=_171;
elem.setAttributeNode(_173);
}
};
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.getEventTarget=function(evt){
if(!evt){
evt=dojo.global().event||{};
}
var t=(evt.srcElement?evt.srcElement:(evt.target?evt.target:null));
while((t)&&(t.nodeType!=1)){
t=t.parentNode;
}
return t;
};
dojo.html.getViewport=function(){
var _176=dojo.global();
var _177=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_177.documentElement.clientWidth;
h=_176.innerHeight;
}else{
if(!dojo.render.html.opera&&_176.innerWidth){
w=_176.innerWidth;
h=_176.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_177,"documentElement.clientWidth")){
var w2=_177.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_177.documentElement.clientHeight;
}else{
if(dojo.body().clientWidth){
w=dojo.body().clientWidth;
h=dojo.body().clientHeight;
}
}
}
}
return {width:w,height:h};
};
dojo.html.getScroll=function(){
var _17b=dojo.global();
var _17c=dojo.doc();
var top=_17b.pageYOffset||_17c.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_17b.pageXOffset||_17c.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _181=dojo.doc();
var _182=dojo.byId(node);
type=type.toLowerCase();
while((_182)&&(_182.nodeName.toLowerCase()!=type)){
if(_182==(_181["body"]||_181["documentElement"])){
return null;
}
_182=_182.parentNode;
}
return _182;
};
dojo.html.getAttribute=function(node,attr){
node=dojo.byId(node);
if((!node)||(!node.getAttribute)){
return null;
}
var ta=typeof attr=="string"?attr:new String(attr);
var v=node.getAttribute(ta.toUpperCase());
if((v)&&(typeof v=="string")&&(v!="")){
return v;
}
if(v&&v.value){
return v.value;
}
if((node.getAttributeNode)&&(node.getAttributeNode(ta))){
return (node.getAttributeNode(ta)).value;
}else{
if(node.getAttribute(ta)){
return node.getAttribute(ta);
}else{
if(node.getAttribute(ta.toLowerCase())){
return node.getAttribute(ta.toLowerCase());
}
}
}
return null;
};
dojo.html.hasAttribute=function(node,attr){
return dojo.html.getAttribute(dojo.byId(node),attr)?true:false;
};
dojo.html.getCursorPosition=function(e){
e=e||dojo.global().event;
var _18a={x:0,y:0};
if(e.pageX||e.pageY){
_18a.x=e.pageX;
_18a.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_18a.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_18a.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _18a;
};
dojo.html.isTag=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName.toLowerCase()==String(arguments[i]).toLowerCase()){
return String(arguments[i]).toLowerCase();
}
}
}
return "";
};
if(dojo.render.html.ie&&!dojo.render.html.ie70){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _18f=dojo.doc().createElement("script");
_18f.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_18f);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_193,uri){
var loc=dojo.hostenv.getModuleSymbols(_193).join("/");
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
var _198=new dojo.uri.Uri(arguments[i].toString());
var _199=new dojo.uri.Uri(uri.toString());
if((_198.path=="")&&(_198.scheme==null)&&(_198.authority==null)&&(_198.query==null)){
if(_198.fragment!=null){
_199.fragment=_198.fragment;
}
_198=_199;
}else{
if(_198.scheme==null){
_198.scheme=_199.scheme;
if(_198.authority==null){
_198.authority=_199.authority;
if(_198.path.charAt(0)!="/"){
var path=_199.path.substring(0,_199.path.lastIndexOf("/")+1)+_198.path;
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
_198.path=segs.join("/");
}
}
}
}
uri="";
if(_198.scheme!=null){
uri+=_198.scheme+":";
}
if(_198.authority!=null){
uri+="//"+_198.authority;
}
uri+=_198.path;
if(_198.query!=null){
uri+="?"+_198.query;
}
if(_198.fragment!=null){
uri+="#"+_198.fragment;
}
}
this.uri=uri.toString();
var _19d="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_19d));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_19d="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_19d));
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
dojo.provide("dojo.html.style");
dojo.html.getClass=function(node){
node=dojo.byId(node);
if(!node){
return "";
}
var cs="";
if(node.className){
cs=node.className;
}else{
if(dojo.html.hasAttribute(node,"class")){
cs=dojo.html.getAttribute(node,"class");
}
}
return cs.replace(/^\s+|\s+$/g,"");
};
dojo.html.getClasses=function(node){
var c=dojo.html.getClass(node);
return (c=="")?[]:c.split(/\s+/g);
};
dojo.html.hasClass=function(node,_1a4){
return (new RegExp("(^|\\s+)"+_1a4+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_1a6){
_1a6+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_1a6);
};
dojo.html.addClass=function(node,_1a8){
if(dojo.html.hasClass(node,_1a8)){
return false;
}
_1a8=(dojo.html.getClass(node)+" "+_1a8).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_1a8);
};
dojo.html.setClass=function(node,_1aa){
node=dojo.byId(node);
var cs=new String(_1aa);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_1aa);
node.className=cs;
}else{
return false;
}
}
}
catch(e){
dojo.debug("dojo.html.setClass() failed",e);
}
return true;
};
dojo.html.removeClass=function(node,_1ad,_1ae){
try{
if(!_1ae){
var _1af=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_1ad+"(\\s+|$)"),"$1$2");
}else{
var _1af=dojo.html.getClass(node).replace(_1ad,"");
}
dojo.html.setClass(node,_1af);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_1b1,_1b2){
dojo.html.removeClass(node,_1b2);
dojo.html.addClass(node,_1b1);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_1b3,_1b4,_1b5,_1b6,_1b7){
_1b7=false;
var _1b8=dojo.doc();
_1b4=dojo.byId(_1b4)||_1b8;
var _1b9=_1b3.split(/\s+/g);
var _1ba=[];
if(_1b6!=1&&_1b6!=2){
_1b6=0;
}
var _1bb=new RegExp("(\\s|^)(("+_1b9.join(")|(")+"))(\\s|$)");
var _1bc=_1b9.join(" ").length;
var _1bd=[];
if(!_1b7&&_1b8.evaluate){
var _1be=".//"+(_1b5||"*")+"[contains(";
if(_1b6!=dojo.html.classMatchType.ContainsAny){
_1be+="concat(' ',@class,' '), ' "+_1b9.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_1b6==2){
_1be+=" and string-length(@class)="+_1bc+"]";
}else{
_1be+="]";
}
}else{
_1be+="concat(' ',@class,' '), ' "+_1b9.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _1bf=_1b8.evaluate(_1be,_1b4,null,XPathResult.ANY_TYPE,null);
var _1c0=_1bf.iterateNext();
while(_1c0){
try{
_1bd.push(_1c0);
_1c0=_1bf.iterateNext();
}
catch(e){
break;
}
}
return _1bd;
}else{
if(!_1b5){
_1b5="*";
}
_1bd=_1b4.getElementsByTagName(_1b5);
var node,i=0;
outer:
while(node=_1bd[i++]){
var _1c3=dojo.html.getClasses(node);
if(_1c3.length==0){
continue outer;
}
var _1c4=0;
for(var j=0;j<_1c3.length;j++){
if(_1bb.test(_1c3[j])){
if(_1b6==dojo.html.classMatchType.ContainsAny){
_1ba.push(node);
continue outer;
}else{
_1c4++;
}
}else{
if(_1b6==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_1c4==_1b9.length){
if((_1b6==dojo.html.classMatchType.IsOnly)&&(_1c4==_1c3.length)){
_1ba.push(node);
}else{
if(_1b6==dojo.html.classMatchType.ContainsAll){
_1ba.push(node);
}
}
}
}
return _1ba;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_1c6){
var arr=_1c6.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_1ca){
return _1ca.replace(/([A-Z])/g,"-$1").toLowerCase();
};
dojo.html.getComputedStyle=function(node,_1cc,_1cd){
node=dojo.byId(node);
var _1cc=dojo.html.toSelectorCase(_1cc);
var _1ce=dojo.html.toCamelCase(_1cc);
if(!node||!node.style){
return _1cd;
}else{
if(document.defaultView&&dojo.html.isDescendantOf(node,node.ownerDocument)){
try{
var cs=document.defaultView.getComputedStyle(node,"");
if(cs){
return cs.getPropertyValue(_1cc);
}
}
catch(e){
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_1cc);
}else{
return _1cd;
}
}
}else{
if(node.currentStyle){
return node.currentStyle[_1ce];
}
}
}
if(node.style.getPropertyValue){
return node.style.getPropertyValue(_1cc);
}else{
return _1cd;
}
};
dojo.html.getStyleProperty=function(node,_1d1){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_1d1)]:undefined);
};
dojo.html.getStyle=function(node,_1d3){
var _1d4=dojo.html.getStyleProperty(node,_1d3);
return (_1d4?_1d4:dojo.html.getComputedStyle(node,_1d3));
};
dojo.html.setStyle=function(node,_1d6,_1d7){
node=dojo.byId(node);
if(node&&node.style){
var _1d8=dojo.html.toCamelCase(_1d6);
node.style[_1d8]=_1d7;
}
};
dojo.html.setStyleText=function(_1d9,text){
try{
_1d9.style.cssText=text;
}
catch(e){
_1d9.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_1db,_1dc){
if(!_1dc.style.cssText){
_1db.setAttribute("style",_1dc.getAttribute("style"));
}else{
_1db.style.cssText=_1dc.style.cssText;
}
dojo.html.addClass(_1db,dojo.html.getClass(_1dc));
};
dojo.html.getUnitValue=function(node,_1de,_1df){
var s=dojo.html.getComputedStyle(node,_1de);
if((!s)||((s=="auto")&&(_1df))){
return {value:0,units:"px"};
}
var _1e1=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_1e1){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_1e1[1]),units:_1e1[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
dojo.html.getPixelValue=function(node,_1e3,_1e4){
var _1e5=dojo.html.getUnitValue(node,_1e3,_1e4);
if(isNaN(_1e5.value)){
return 0;
}
if((_1e5.value)&&(_1e5.units!="px")){
return NaN;
}
return _1e5.value;
};
dojo.html.setPositivePixelValue=function(node,_1e7,_1e8){
if(isNaN(_1e8)){
return false;
}
node.style[_1e7]=Math.max(0,_1e8)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_1e9,_1ea,_1eb){
if(!dojo.html.styleSheet){
if(document.createStyleSheet){
dojo.html.styleSheet=document.createStyleSheet();
}else{
if(document.styleSheets[0]){
dojo.html.styleSheet=document.styleSheets[0];
}else{
return null;
}
}
}
if(arguments.length<3){
if(dojo.html.styleSheet.cssRules){
_1eb=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_1eb=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_1e9+" { "+_1ea+" }";
return dojo.html.styleSheet.insertRule(rule,_1eb);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_1e9,_1ea,_1eb);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_1ed){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_1ed){
_1ed=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_1ed);
}
}else{
if(document.styleSheets[0]){
if(!_1ed){
_1ed=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_1ed);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_1f0,_1f1){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _1f2=dojo.hostenv.getText(URI,false,_1f1);
if(_1f2===null){
return;
}
_1f2=dojo.html.fixPathsInCssText(_1f2,URI);
if(_1f0){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_1f2)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _1f7=doc.getElementsByTagName("style");
for(var i=0;i<_1f7.length;i++){
if(_1f7[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _1f8=dojo.html.insertCssText(_1f2,doc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_1f2,"nodeRef":_1f8});
if(_1f8&&djConfig.isDebug){
_1f8.setAttribute("dbgHref",URI);
}
return _1f8;
};
dojo.html.insertCssText=function(_1f9,doc,URI){
if(!_1f9){
return;
}
if(!doc){
doc=document;
}
if(URI){
_1f9=dojo.html.fixPathsInCssText(_1f9,URI);
}
var _1fc=doc.createElement("style");
_1fc.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_1fc);
}
if(_1fc.styleSheet){
var _1fe=function(){
try{
_1fc.styleSheet.cssText=_1f9;
}
catch(e){
dojo.debug(e);
}
};
if(_1fc.styleSheet.disabled){
setTimeout(_1fe,10);
}else{
_1fe();
}
}else{
var _1ff=doc.createTextNode(_1f9);
_1fc.appendChild(_1ff);
}
return _1fc;
};
dojo.html.fixPathsInCssText=function(_200,URI){
if(!_200||!URI){
return;
}
var _202,str="",url="",_205="[\\t\\s\\w\\(\\)\\/\\.\\\\'\"-:#=&?~]+";
var _206=new RegExp("url\\(\\s*("+_205+")\\s*\\)");
var _207=/(file|https?|ftps?):\/\//;
regexTrim=new RegExp("^[\\s]*(['\"]?)("+_205+")\\1[\\s]*?$");
if(dojo.render.html.ie55||dojo.render.html.ie60){
var _208=new RegExp("AlphaImageLoader\\((.*)src=['\"]("+_205+")['\"]");
while(_202=_208.exec(_200)){
url=_202[2].replace(regexTrim,"$2");
if(!_207.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_200.substring(0,_202.index)+"AlphaImageLoader("+_202[1]+"src='"+url+"'";
_200=_200.substr(_202.index+_202[0].length);
}
_200=str+_200;
str="";
}
while(_202=_206.exec(_200)){
url=_202[1].replace(regexTrim,"$2");
if(!_207.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_200.substring(0,_202.index)+"url("+url+")";
_200=_200.substr(_202.index+_202[0].length);
}
return str+_200;
};
dojo.html.setActiveStyleSheet=function(_209){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_209){
a.disabled=false;
}
}
}
};
dojo.html.getActiveStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")&&!a.disabled){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.getPreferredStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("rel").indexOf("alt")==-1&&a.getAttribute("title")){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.applyBrowserClass=function(node){
var drh=dojo.render.html;
var _215={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _215){
if(_215[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.provide("dojo.html.*");
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_218,_219){
node=dojo.byId(node);
_219(node,!_218(node));
return _218(node);
};
dojo.html.show=function(node){
node=dojo.byId(node);
if(dojo.html.getStyleProperty(node,"display")=="none"){
var _21b=dojo.html.getAttribute("djDisplayCache");
dojo.html.setStyle(node,"display",(_21b||""));
node.removeAttribute("djDisplayCache");
}
};
dojo.html.hide=function(node){
node=dojo.byId(node);
var _21d=dojo.html.getAttribute("djDisplayCache");
if(_21d==null){
var d=dojo.html.getStyleProperty(node,"display");
if(d!="none"){
node.setAttribute("djDisplayCache",d);
}
}
dojo.html.setStyle(node,"display","none");
};
dojo.html.setShowing=function(node,_220){
dojo.html[(_220?"show":"hide")](node);
};
dojo.html.isShowing=function(node){
return (dojo.html.getStyleProperty(node,"display")!="none");
};
dojo.html.toggleShowing=function(node){
return dojo.html._toggle(node,dojo.html.isShowing,dojo.html.setShowing);
};
dojo.html.displayMap={tr:"",td:"",th:"",img:"inline",span:"inline",input:"inline",button:"inline"};
dojo.html.suggestDisplayByTagName=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
var tag=node.tagName.toLowerCase();
return (tag in dojo.html.displayMap?dojo.html.displayMap[tag]:"block");
}
};
dojo.html.setDisplay=function(node,_226){
dojo.html.setStyle(node,"display",((_226 instanceof String||typeof _226=="string")?_226:(_226?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_22a){
dojo.html.setStyle(node,"visibility",((_22a instanceof String||typeof _22a=="string")?_22a:(_22a?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_22e,_22f){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_22f){
if(_22e>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_22e=0.999999;
}
}else{
if(_22e<0){
_22e=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_22e*100+")";
}
}
node.style.filter="Alpha(Opacity="+_22e*100+")";
}else{
if(h.moz){
node.style.opacity=_22e;
node.style.MozOpacity=_22e;
}else{
if(h.safari){
node.style.opacity=_22e;
node.style.KhtmlOpacity=_22e;
}else{
node.style.opacity=_22e;
}
}
}
};
dojo.html.clearOpacity=function(node){
node=dojo.byId(node);
var ns=node.style;
var h=dojo.render.html;
if(h.ie){
try{
if(node.filters&&node.filters.alpha){
ns.filter="";
}
}
catch(e){
}
}else{
if(h.moz){
ns.opacity=1;
ns.MozOpacity=1;
}else{
if(h.safari){
ns.opacity=1;
ns.KhtmlOpacity=1;
}else{
ns.opacity=1;
}
}
}
};
dojo.html.getOpacity=function(node){
node=dojo.byId(node);
var h=dojo.render.html;
if(h.ie){
var opac=(node.filters&&node.filters.alpha&&typeof node.filters.alpha.opacity=="number"?node.filters.alpha.opacity:100)/100;
}else{
var opac=node.style.opacity||node.style.MozOpacity||node.style.KhtmlOpacity||1;
}
return opac>=0.999999?1:Number(opac);
};
dojo.provide("dojo.html.layout");
dojo.html.sumAncestorProperties=function(node,prop){
node=dojo.byId(node);
if(!node){
return 0;
}
var _23b=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_23b+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _23b;
};
dojo.html.setStyleAttributes=function(node,_23e){
node=dojo.byId(node);
var _23f=_23e.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_23f.length;i++){
var _241=_23f[i].split(":");
var name=_241[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _243=_241[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_243);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_243});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_243});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_243});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_243});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_243;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_245,_246){
node=dojo.byId(node);
var _247=dojo.doc();
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_246){
_246=bs.CONTENT_BOX;
}
var _24a=2;
var _24b;
switch(_246){
case bs.MARGIN_BOX:
_24b=3;
break;
case bs.BORDER_BOX:
_24b=2;
break;
case bs.PADDING_BOX:
default:
_24b=1;
break;
case bs.CONTENT_BOX:
_24b=0;
break;
}
var h=dojo.render.html;
var db=_247["body"]||_247["documentElement"];
if(h.ie){
with(node.getBoundingClientRect()){
ret.x=left-2;
ret.y=top-2;
}
}else{
if(_247["getBoxObjectFor"]){
_24a=1;
try{
var bo=_247.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _24f;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_24f=db;
}else{
_24f=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _251=node;
do{
var n=_251["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_251["offsetTop"];
ret.y+=isNaN(m)?0:m;
_251=_251.offsetParent;
}while((_251!=_24f)&&(_251!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_245){
var _254=dojo.html.getScroll();
ret.y+=_254.top;
ret.x+=_254.left;
}
var _255=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_24a>_24b){
for(var i=_24b;i<_24a;++i){
ret.y+=_255[i](node,"top");
ret.x+=_255[i](node,"left");
}
}else{
if(_24a<_24b){
for(var i=_24b;i>_24a;--i){
ret.y-=_255[i-1](node,"top");
ret.x-=_255[i-1](node,"left");
}
}
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.isPositionAbsolute=function(node){
return (dojo.html.getComputedStyle(node,"position")=="absolute");
};
dojo.html._getComponentPixelValues=function(node,_259,_25a,_25b){
var _25c=["top","bottom","left","right"];
var obj={};
for(var i in _25c){
side=_25c[i];
obj[side]=_25a(node,_259+side,_25b);
}
obj.width=obj.left+obj.right;
obj.height=obj.top+obj.bottom;
return obj;
};
dojo.html.getMargin=function(node){
return dojo.html._getComponentPixelValues(node,"margin-",dojo.html.getPixelValue,dojo.html.isPositionAbsolute(node));
};
dojo.html.getBorder=function(node){
return dojo.html._getComponentPixelValues(node,"",dojo.html.getBorderExtent);
};
dojo.html.getBorderExtent=function(node,side){
return (dojo.html.getStyle(node,"border-"+side+"-style")=="none"?0:dojo.html.getPixelValue(node,"border-"+side+"-width"));
};
dojo.html.getMarginExtent=function(node,side){
return dojo.html.getPixelValue(node,"margin-"+side,dojo.html.isPositionAbsolute(node));
};
dojo.html.getPaddingExtent=function(node,side){
return dojo.html.getPixelValue(node,"padding-"+side,true);
};
dojo.html.getPadding=function(node){
return dojo.html._getComponentPixelValues(node,"padding-",dojo.html.getPixelValue,true);
};
dojo.html.getPadBorder=function(node){
var pad=dojo.html.getPadding(node);
var _26a=dojo.html.getBorder(node);
return {width:pad.width+_26a.width,height:pad.height+_26a.height};
};
dojo.html.getBoxSizing=function(node){
var h=dojo.render.html;
var bs=dojo.html.boxSizing;
if(((h.ie)||(h.opera))&&node.nodeName!="IMG"){
var cm=document["compatMode"];
if((cm=="BackCompat")||(cm=="QuirksMode")){
return bs.BORDER_BOX;
}else{
return bs.CONTENT_BOX;
}
}else{
if(arguments.length==0){
node=document.documentElement;
}
var _26f=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_26f){
_26f=dojo.html.getStyle(node,"box-sizing");
}
return (_26f?_26f:bs.CONTENT_BOX);
}
};
dojo.html.isBorderBox=function(node){
return (dojo.html.getBoxSizing(node)==dojo.html.boxSizing.BORDER_BOX);
};
dojo.html.getBorderBox=function(node){
node=dojo.byId(node);
return {width:node.offsetWidth,height:node.offsetHeight};
};
dojo.html.getPaddingBox=function(node){
var box=dojo.html.getBorderBox(node);
var _274=dojo.html.getBorder(node);
return {width:box.width-_274.width,height:box.height-_274.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _276=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_276.width,height:node.offsetHeight-_276.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _279=0;
var _27a=0;
var isbb=dojo.html.isBorderBox(node);
var _27c=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_279=args.width+_27c.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_279);
}
if(typeof args.height!="undefined"){
_27a=args.height+_27c.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_27a);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _27f=dojo.html.getBorderBox(node);
var _280=dojo.html.getMargin(node);
return {width:_27f.width+_280.width,height:_27f.height+_280.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _283=0;
var _284=0;
var isbb=dojo.html.isBorderBox(node);
var _286=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _287=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_283=args.width-_286.width;
_283-=_287.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_283);
}
if(typeof args.height!="undefined"){
_284=args.height-_286.height;
_284-=_287.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_284);
}
return ret;
};
dojo.html.getElementBox=function(node,type){
var bs=dojo.html.boxSizing;
switch(type){
case bs.MARGIN_BOX:
return dojo.html.getMarginBox(node);
case bs.BORDER_BOX:
return dojo.html.getBorderBox(node);
case bs.PADDING_BOX:
return dojo.html.getPaddingBox(node);
case bs.CONTENT_BOX:
default:
return dojo.html.getContentBox(node);
}
};
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_28c,_28d,_28e){
if(!_28c.nodeType&&!(_28c instanceof String||typeof _28c=="string")&&("width" in _28c||"height" in _28c||"left" in _28c||"x" in _28c||"top" in _28c||"y" in _28c)){
var ret={left:_28c.left||_28c.x||0,top:_28c.top||_28c.y||0,width:_28c.width||0,height:_28c.height||0};
}else{
var node=dojo.byId(_28c);
var pos=dojo.html.abs(node,_28d,_28e);
var _292=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_292.width,height:_292.height};
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_294){
return dojo.html._callDeprecated("setMarginBoxWidth","setMarginBox",arguments,"width");
};
dojo.html.setMarginBoxHeight=dojo.html.setOuterHeight=function(){
return dojo.html._callDeprecated("setMarginBoxHeight","setMarginBox",arguments,"height");
};
dojo.html.getMarginBoxWidth=dojo.html.getOuterWidth=function(){
return dojo.html._callDeprecated("getMarginBoxWidth","getMarginBox",arguments,null,"width");
};
dojo.html.getMarginBoxHeight=dojo.html.getOuterHeight=function(){
return dojo.html._callDeprecated("getMarginBoxHeight","getMarginBox",arguments,null,"height");
};
dojo.html.getTotalOffset=function(node,type,_297){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_299){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_29b){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_29d){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_29f){
return dojo.html._callDeprecated("totalOffsetTop","getAbsolutePosition",arguments,null,"top");
};
dojo.html.getMarginWidth=function(node){
return dojo.html._callDeprecated("getMarginWidth","getMargin",arguments,null,"width");
};
dojo.html.getMarginHeight=function(node){
return dojo.html._callDeprecated("getMarginHeight","getMargin",arguments,null,"height");
};
dojo.html.getBorderWidth=function(node){
return dojo.html._callDeprecated("getBorderWidth","getBorder",arguments,null,"width");
};
dojo.html.getBorderHeight=function(node){
return dojo.html._callDeprecated("getBorderHeight","getBorder",arguments,null,"height");
};
dojo.html.getPaddingWidth=function(node){
return dojo.html._callDeprecated("getPaddingWidth","getPadding",arguments,null,"width");
};
dojo.html.getPaddingHeight=function(node){
return dojo.html._callDeprecated("getPaddingHeight","getPadding",arguments,null,"height");
};
dojo.html.getPadBorderWidth=function(node){
return dojo.html._callDeprecated("getPadBorderWidth","getPadBorder",arguments,null,"width");
};
dojo.html.getPadBorderHeight=function(node){
return dojo.html._callDeprecated("getPadBorderHeight","getPadBorder",arguments,null,"height");
};
dojo.html.getBorderBoxWidth=dojo.html.getInnerWidth=function(){
return dojo.html._callDeprecated("getBorderBoxWidth","getBorderBox",arguments,null,"width");
};
dojo.html.getBorderBoxHeight=dojo.html.getInnerHeight=function(){
return dojo.html._callDeprecated("getBorderBoxHeight","getBorderBox",arguments,null,"height");
};
dojo.html.getContentBoxWidth=dojo.html.getContentWidth=function(){
return dojo.html._callDeprecated("getContentBoxWidth","getContentBox",arguments,null,"width");
};
dojo.html.getContentBoxHeight=dojo.html.getContentHeight=function(){
return dojo.html._callDeprecated("getContentBoxHeight","getContentBox",arguments,null,"height");
};
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_2a9){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_2ab){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.html.util");
dojo.html.getElementWindow=function(_2ac){
return dojo.html.getDocumentWindow(_2ac.ownerDocument);
};
dojo.html.getDocumentWindow=function(doc){
if(dojo.render.html.safari&&!doc._parentWindow){
var fix=function(win){
win.document._parentWindow=win;
for(var i=0;i<win.frames.length;i++){
fix(win.frames[i]);
}
};
fix(window.top);
}
if(dojo.render.html.ie&&window!==document.parentWindow&&!doc._parentWindow){
doc.parentWindow.execScript("document._parentWindow = window;","Javascript");
var win=doc._parentWindow;
doc._parentWindow=null;
return win;
}
return doc._parentWindow||doc.parentWindow||doc.defaultView;
};
dojo.html.getAbsolutePositionExt=function(node,_2b3,_2b4,_2b5){
var _2b6=dojo.html.getElementWindow(node);
var ret=dojo.withGlobal(_2b6,"getAbsolutePosition",dojo.html,arguments);
var win=dojo.html.getElementWindow(node);
if(_2b5!=win&&win.frameElement){
var ext=dojo.html.getAbsolutePositionExt(win.frameElement,_2b3,_2b4,_2b5);
ret.x+=ext.x;
ret.y+=ext.y;
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.gravity=function(node,e){
node=dojo.byId(node);
var _2bc=dojo.html.getCursorPosition(e);
with(dojo.html){
var _2bd=getAbsolutePosition(node,true);
var bb=getBorderBox(node);
var _2bf=_2bd.x+(bb.width/2);
var _2c0=_2bd.y+(bb.height/2);
}
with(dojo.html.gravity){
return ((_2bc.x<_2bf?WEST:EAST)|(_2bc.y<_2c0?NORTH:SOUTH));
}
};
dojo.html.gravity.NORTH=1;
dojo.html.gravity.SOUTH=1<<1;
dojo.html.gravity.EAST=1<<2;
dojo.html.gravity.WEST=1<<3;
dojo.html.overElement=function(_2c1,e){
_2c1=dojo.byId(_2c1);
var _2c3=dojo.html.getCursorPosition(e);
var bb=dojo.html.getBorderBox(_2c1);
var _2c5=dojo.html.getAbsolutePosition(_2c1,true,dojo.html.boxSizing.BORDER_BOX);
var top=_2c5.y;
var _2c7=top+bb.height;
var left=_2c5.x;
var _2c9=left+bb.width;
return (_2c3.x>=left&&_2c3.x<=_2c9&&_2c3.y>=top&&_2c3.y<=_2c7);
};
dojo.html.renderedTextContent=function(node){
node=dojo.byId(node);
var _2cb="";
if(node==null){
return _2cb;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
var _2cd="unknown";
try{
_2cd=dojo.html.getStyle(node.childNodes[i],"display");
}
catch(E){
}
switch(_2cd){
case "block":
case "list-item":
case "run-in":
case "table":
case "table-row-group":
case "table-header-group":
case "table-footer-group":
case "table-row":
case "table-column-group":
case "table-column":
case "table-cell":
case "table-caption":
_2cb+="\n";
_2cb+=dojo.html.renderedTextContent(node.childNodes[i]);
_2cb+="\n";
break;
case "none":
break;
default:
if(node.childNodes[i].tagName&&node.childNodes[i].tagName.toLowerCase()=="br"){
_2cb+="\n";
}else{
_2cb+=dojo.html.renderedTextContent(node.childNodes[i]);
}
break;
}
break;
case 3:
case 2:
case 4:
var text=node.childNodes[i].nodeValue;
var _2cf="unknown";
try{
_2cf=dojo.html.getStyle(node,"text-transform");
}
catch(E){
}
switch(_2cf){
case "capitalize":
var _2d0=text.split(" ");
for(var i=0;i<_2d0.length;i++){
_2d0[i]=_2d0[i].charAt(0).toUpperCase()+_2d0[i].substring(1);
}
text=_2d0.join(" ");
break;
case "uppercase":
text=text.toUpperCase();
break;
case "lowercase":
text=text.toLowerCase();
break;
default:
break;
}
switch(_2cf){
case "nowrap":
break;
case "pre-wrap":
break;
case "pre-line":
break;
case "pre":
break;
default:
text=text.replace(/\s+/," ");
if(/\s$/.test(_2cb)){
text.replace(/^\s/,"");
}
break;
}
_2cb+=text;
break;
default:
break;
}
}
return _2cb;
};
dojo.html.createNodesFromText=function(txt,trim){
if(trim){
txt=txt.replace(/^\s+|\s+$/g,"");
}
var tn=dojo.doc().createElement("div");
tn.style.visibility="hidden";
dojo.body().appendChild(tn);
var _2d4="none";
if((/^<t[dh][\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody><tr>"+txt+"</tr></tbody></table>";
_2d4="cell";
}else{
if((/^<tr[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody>"+txt+"</tbody></table>";
_2d4="row";
}else{
if((/^<(thead|tbody|tfoot)[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table>"+txt+"</table>";
_2d4="section";
}
}
}
tn.innerHTML=txt;
if(tn["normalize"]){
tn.normalize();
}
var _2d5=null;
switch(_2d4){
case "cell":
_2d5=tn.getElementsByTagName("tr")[0];
break;
case "row":
_2d5=tn.getElementsByTagName("tbody")[0];
break;
case "section":
_2d5=tn.getElementsByTagName("table")[0];
break;
default:
_2d5=tn;
break;
}
var _2d6=[];
for(var x=0;x<_2d5.childNodes.length;x++){
_2d6.push(_2d5.childNodes[x].cloneNode(true));
}
tn.style.display="none";
dojo.html.destroyNode(tn);
return _2d6;
};
dojo.html.placeOnScreen=function(node,_2d9,_2da,_2db,_2dc,_2dd,_2de){
if(_2d9 instanceof Array||typeof _2d9=="array"){
_2de=_2dd;
_2dd=_2dc;
_2dc=_2db;
_2db=_2da;
_2da=_2d9[1];
_2d9=_2d9[0];
}
if(_2dd instanceof String||typeof _2dd=="string"){
_2dd=_2dd.split(",");
}
if(!isNaN(_2db)){
_2db=[Number(_2db),Number(_2db)];
}else{
if(!(_2db instanceof Array||typeof _2db=="array")){
_2db=[0,0];
}
}
var _2df=dojo.html.getScroll().offset;
var view=dojo.html.getViewport();
node=dojo.byId(node);
var _2e1=node.style.display;
node.style.display="";
var bb=dojo.html.getBorderBox(node);
var w=bb.width;
var h=bb.height;
node.style.display=_2e1;
if(!(_2dd instanceof Array||typeof _2dd=="array")){
_2dd=["TL"];
}
var _2e5,_2e6,_2e7=Infinity,_2e8;
for(var _2e9=0;_2e9<_2dd.length;++_2e9){
var _2ea=_2dd[_2e9];
var _2eb=true;
var tryX=_2d9-(_2ea.charAt(1)=="L"?0:w)+_2db[0]*(_2ea.charAt(1)=="L"?1:-1);
var tryY=_2da-(_2ea.charAt(0)=="T"?0:h)+_2db[1]*(_2ea.charAt(0)=="T"?1:-1);
if(_2dc){
tryX-=_2df.x;
tryY-=_2df.y;
}
if(tryX<0){
tryX=0;
_2eb=false;
}
if(tryY<0){
tryY=0;
_2eb=false;
}
var x=tryX+w;
if(x>view.width){
x=view.width-w;
_2eb=false;
}else{
x=tryX;
}
x=Math.max(_2db[0],x)+_2df.x;
var y=tryY+h;
if(y>view.height){
y=view.height-h;
_2eb=false;
}else{
y=tryY;
}
y=Math.max(_2db[1],y)+_2df.y;
if(_2eb){
_2e5=x;
_2e6=y;
_2e7=0;
_2e8=_2ea;
break;
}else{
var dist=Math.pow(x-tryX-_2df.x,2)+Math.pow(y-tryY-_2df.y,2);
if(_2e7>dist){
_2e7=dist;
_2e5=x;
_2e6=y;
_2e8=_2ea;
}
}
}
if(!_2de){
node.style.left=_2e5+"px";
node.style.top=_2e6+"px";
}
return {left:_2e5,top:_2e6,x:_2e5,y:_2e6,dist:_2e7,corner:_2e8};
};
dojo.html.placeOnScreenAroundElement=function(node,_2f2,_2f3,_2f4,_2f5,_2f6){
var best,_2f8=Infinity;
_2f2=dojo.byId(_2f2);
var _2f9=_2f2.style.display;
_2f2.style.display="";
var mb=dojo.html.getElementBox(_2f2,_2f4);
var _2fb=mb.width;
var _2fc=mb.height;
var _2fd=dojo.html.getAbsolutePosition(_2f2,true,_2f4);
_2f2.style.display=_2f9;
for(var _2fe in _2f5){
var pos,_300,_301;
var _302=_2f5[_2fe];
_300=_2fd.x+(_2fe.charAt(1)=="L"?0:_2fb);
_301=_2fd.y+(_2fe.charAt(0)=="T"?0:_2fc);
pos=dojo.html.placeOnScreen(node,_300,_301,_2f3,true,_302,true);
if(pos.dist==0){
best=pos;
break;
}else{
if(_2f8>pos.dist){
_2f8=pos.dist;
best=pos;
}
}
}
if(!_2f6){
node.style.left=best.left+"px";
node.style.top=best.top+"px";
}
return best;
};
dojo.html.scrollIntoView=function(node){
if(!node){
return;
}
if(dojo.render.html.ie){
if(dojo.html.getBorderBox(node.parentNode).height<=node.parentNode.scrollHeight){
node.scrollIntoView(false);
}
}else{
if(dojo.render.html.mozilla){
node.scrollIntoView(false);
}else{
var _304=node.parentNode;
var _305=_304.scrollTop+dojo.html.getBorderBox(_304).height;
var _306=node.offsetTop+dojo.html.getMarginBox(node).height;
if(_305<_306){
_304.scrollTop+=(_306-_305);
}else{
if(_304.scrollTop>node.offsetTop){
_304.scrollTop-=(_304.scrollTop-node.offsetTop);
}
}
}
}
};
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length===0;
}else{
if(dojo.lang.isObject(obj)){
var tmp={};
for(var x in obj){
if(obj[x]&&(!tmp[x])){
return false;
}
}
return true;
}
}
},map:function(arr,obj,_30e){
var _30f=dojo.lang.isString(arr);
if(_30f){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_30e)){
_30e=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_30e){
var _310=obj;
obj=_30e;
_30e=_310;
}
}
if(Array.map){
var _311=Array.map(arr,_30e,obj);
}else{
var _311=[];
for(var i=0;i<arr.length;++i){
_311.push(_30e.call(obj,arr[i]));
}
}
if(_30f){
return _311.join("");
}else{
return _311;
}
},reduce:function(arr,_314,obj,_316){
var _317=_314;
if(arguments.length==1){
dojo.debug("dojo.lang.reduce called with too few arguments!");
return false;
}else{
if(arguments.length==2){
_316=_314;
_317=arr.shift();
}else{
if(arguments.lenght==3){
if(dojo.lang.isFunction(obj)){
_316=obj;
obj=null;
}
}else{
if(dojo.lang.isFunction(obj)){
var tmp=_316;
_316=obj;
obj=tmp;
}
}
}
}
var ob=obj?obj:dj_global;
dojo.lang.map(arr,function(val){
_317=_316.call(ob,_317,val);
});
return _317;
},forEach:function(_31b,_31c,_31d){
if(dojo.lang.isString(_31b)){
_31b=_31b.split("");
}
if(Array.forEach){
Array.forEach(_31b,_31c,_31d);
}else{
if(!_31d){
_31d=dj_global;
}
for(var i=0,l=_31b.length;i<l;i++){
_31c.call(_31d,_31b[i],i,_31b);
}
}
},_everyOrSome:function(_320,arr,_322,_323){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_320?"every":"some"](arr,_322,_323);
}else{
if(!_323){
_323=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _326=_322.call(_323,arr[i],i,arr);
if(_320&&!_326){
return false;
}else{
if((!_320)&&(_326)){
return true;
}
}
}
return Boolean(_320);
}
},every:function(arr,_328,_329){
return this._everyOrSome(true,arr,_328,_329);
},some:function(arr,_32b,_32c){
return this._everyOrSome(false,arr,_32b,_32c);
},filter:function(arr,_32e,_32f){
var _330=dojo.lang.isString(arr);
if(_330){
arr=arr.split("");
}
var _331;
if(Array.filter){
_331=Array.filter(arr,_32e,_32f);
}else{
if(!_32f){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_32f=dj_global;
}
_331=[];
for(var i=0;i<arr.length;i++){
if(_32e.call(_32f,arr[i],i,arr)){
_331.push(arr[i]);
}
}
}
if(_330){
return _331.join("");
}else{
return _331;
}
},unnest:function(){
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
},toArray:function(_336,_337){
var _338=[];
for(var i=_337||0;i<_336.length;i++){
_338.push(_336[i]);
}
return _338;
}});
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_340){
if(_340){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_341,_342){
var rgb=null;
if(dojo.lang.isArray(_341)){
rgb=_341;
}else{
if(_341 instanceof dojo.gfx.color.Color){
rgb=_341.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_341).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_342);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_346){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_346);
}
if(!_346){
_346=0;
}
_346=Math.min(Math.max(-1,_346),1);
_346=((_346+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_346));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_34b){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_34b));
};
dojo.gfx.color.extractRGB=function(_34c){
_34c=_34c.toLowerCase();
if(_34c.indexOf("rgb")==0){
var _34d=_34c.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_34d.splice(1,3);
return ret;
}else{
var _34f=dojo.gfx.color.hex2rgb(_34c);
if(_34f){
return _34f;
}else{
return dojo.gfx.color.named[_34c]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _351="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_351+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_351.indexOf(rgb[i].charAt(0))*16+_351.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_35a,_35b){
var args=[];
for(var x=2;x<arguments.length;x++){
args.push(arguments[x]);
}
var fcn=(dojo.lang.isString(_35b)?_35a[_35b]:_35b)||function(){
};
return function(){
var ta=args.concat([]);
for(var x=0;x<arguments.length;x++){
ta.push(arguments[x]);
}
return fcn.apply(_35a,ta);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_361,_362,_363){
var nso=(_362||dojo.lang.anon);
if((_363)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_361){
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
nso[ret]=_361;
return ret;
};
dojo.lang.forward=function(_367){
return function(){
return this[_367].apply(this,arguments);
};
};
dojo.lang.curry=function(_368,func){
var _36a=[];
_368=_368||dj_global;
if(dojo.lang.isString(func)){
func=_368[func];
}
for(var x=2;x<arguments.length;x++){
_36a.push(arguments[x]);
}
var _36c=(func["__preJoinArity"]||func.length)-_36a.length;
function gather(_36d,_36e,_36f){
var _370=_36f;
var _371=_36e.slice(0);
for(var x=0;x<_36d.length;x++){
_371.push(_36d[x]);
}
_36f=_36f-_36d.length;
if(_36f<=0){
var res=func.apply(_368,_371);
_36f=_370;
return res;
}else{
return function(){
return gather(arguments,_371,_36f);
};
}
}
return gather([],_36a,_36c);
};
dojo.lang.curryArguments=function(_374,func,args,_377){
var _378=[];
var x=_377||0;
for(x=_377;x<args.length;x++){
_378.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_374,func].concat(_378));
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
dojo.lang.delayThese=function(farr,cb,_37e,_37f){
if(!farr.length){
if(typeof _37f=="function"){
_37f();
}
return;
}
if((typeof _37e=="undefined")&&(typeof cb=="number")){
_37e=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_37e){
_37e=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_37e,_37f);
},_37e);
};
dojo.provide("dojo.lfx.Animation");
dojo.lfx.Line=function(_380,end){
this.start=_380;
this.end=end;
if(dojo.lang.isArray(_380)){
var diff=[];
dojo.lang.forEach(this.start,function(s,i){
diff[i]=this.end[i]-s;
},this);
this.getValue=function(n){
var res=[];
dojo.lang.forEach(this.start,function(s,i){
res[i]=(diff[i]*n)+s;
},this);
return res;
};
}else{
var diff=end-_380;
this.getValue=function(n){
return (diff*n)+this.start;
};
}
};
dojo.lfx.easeDefault=function(n){
if(dojo.render.html.khtml){
return (parseFloat("0.5")+((Math.sin((n+parseFloat("1.5"))*Math.PI))/2));
}else{
return (0.5+((Math.sin((n+1.5)*Math.PI))/2));
}
};
dojo.lfx.easeIn=function(n){
return Math.pow(n,3);
};
dojo.lfx.easeOut=function(n){
return (1-Math.pow(1-n,3));
};
dojo.lfx.easeInOut=function(n){
return ((3*Math.pow(n,2))-(2*Math.pow(n,3)));
};
dojo.lfx.IAnimation=function(){
};
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:25,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_38f,_390){
if(!_390){
_390=_38f;
_38f=this;
}
_390=dojo.lang.hitch(_38f,_390);
var _391=this[evt]||function(){
};
this[evt]=function(){
var ret=_391.apply(this,arguments);
_390.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_395){
this.repeatCount=_395;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_396,_397,_398,_399,_39a,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_396)||(!_396&&_397.getValue)){
rate=_39a;
_39a=_399;
_399=_398;
_398=_397;
_397=_396;
_396=null;
}else{
if(_396.getValue||dojo.lang.isArray(_396)){
rate=_399;
_39a=_398;
_399=_397;
_398=_396;
_397=null;
_396=null;
}
}
if(dojo.lang.isArray(_398)){
this.curve=new dojo.lfx.Line(_398[0],_398[1]);
}else{
this.curve=_398;
}
if(_397!=null&&_397>0){
this.duration=_397;
}
if(_39a){
this.repeatCount=_39a;
}
if(rate){
this.rate=rate;
}
if(_396){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_396[item]){
this.connect(item,_396[item]);
}
},this);
}
if(_399&&dojo.lang.isFunction(_399)){
this.easing=_399;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_39d,_39e){
if(_39e){
clearTimeout(this._timer);
this._active=false;
this._paused=false;
this._percent=0;
}else{
if(this._active&&!this._paused){
return this;
}
}
this.fire("handler",["beforeBegin"]);
this.fire("beforeBegin");
if(_39d>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_39e);
}),_39d);
return this;
}
this._startTime=new Date().valueOf();
if(this._paused){
this._startTime-=(this.duration*this._percent/100);
}
this._endTime=this._startTime+this.duration;
this._active=true;
this._paused=false;
var step=this._percent/100;
var _3a0=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_3a0]);
this.fire("onBegin",[_3a0]);
}
this.fire("handler",["play",_3a0]);
this.fire("onPlay",[_3a0]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _3a1=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_3a1]);
this.fire("onPause",[_3a1]);
return this;
},gotoPercent:function(pct,_3a3){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_3a3){
this.play();
}
return this;
},stop:function(_3a4){
clearTimeout(this._timer);
var step=this._percent/100;
if(_3a4){
step=1;
}
var _3a6=this.curve.getValue(step);
this.fire("handler",["stop",_3a6]);
this.fire("onStop",[_3a6]);
this._active=false;
this._paused=false;
return this;
},status:function(){
if(this._active){
return this._paused?"paused":"playing";
}else{
return "stopped";
}
return this;
},_cycle:function(){
clearTimeout(this._timer);
if(this._active){
var curr=new Date().valueOf();
var step=(curr-this._startTime)/(this._endTime-this._startTime);
if(step>=1){
step=1;
this._percent=100;
}else{
this._percent=step*100;
}
if((this.easing)&&(dojo.lang.isFunction(this.easing))){
step=this.easing(step);
}
var _3a9=this.curve.getValue(step);
this.fire("handler",["animate",_3a9]);
this.fire("onAnimate",[_3a9]);
if(step<1){
this._timer=setTimeout(dojo.lang.hitch(this,"_cycle"),this.rate);
}else{
this._active=false;
this.fire("handler",["end"]);
this.fire("onEnd");
if(this.repeatCount>0){
this.repeatCount--;
this.play(null,true);
}else{
if(this.repeatCount==-1){
this.play(null,true);
}else{
if(this._startRepeatCount){
this.repeatCount=this._startRepeatCount;
this._startRepeatCount=0;
}
}
}
}
}
return this;
}});
dojo.lfx.Combine=function(_3aa){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _3ab=arguments;
if(_3ab.length==1&&(dojo.lang.isArray(_3ab[0])||dojo.lang.isArrayLike(_3ab[0]))){
_3ab=_3ab[0];
}
dojo.lang.forEach(_3ab,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_3ad,_3ae){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_3ad>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_3ae);
}),_3ad);
return this;
}
if(_3ae||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_3ae);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_3af){
this.fire("onStop");
this._animsCall("stop",_3af);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_3b0){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _3b3=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_3b0](args);
},_3b3);
return this;
}});
dojo.lfx.Chain=function(_3b5){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _3b6=arguments;
if(_3b6.length==1&&(dojo.lang.isArray(_3b6[0])||dojo.lang.isArrayLike(_3b6[0]))){
_3b6=_3b6[0];
}
var _3b7=this;
dojo.lang.forEach(_3b6,function(anim,i,_3ba){
this._anims.push(anim);
if(i<_3ba.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_3bb,_3bc){
if(!this._anims.length){
return this;
}
if(_3bc||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _3bd=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_3bb>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_3bc);
}),_3bb);
return this;
}
if(_3bd){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_3bd.play(null,_3bc);
}
return this;
},pause:function(){
if(this._anims[this._currAnim]){
this._anims[this._currAnim].pause();
this.fire("onPause",[this._currAnim]);
}
return this;
},playPause:function(){
if(this._anims.length==0){
return this;
}
if(this._currAnim==-1){
this._currAnim=0;
}
var _3be=this._anims[this._currAnim];
if(_3be){
if(!_3be._active||_3be._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _3bf=this._anims[this._currAnim];
if(_3bf){
_3bf.stop();
this.fire("onStop",[this._currAnim]);
}
return _3bf;
},_playNext:function(){
if(this._currAnim==-1||this._anims.length==0){
return this;
}
this._currAnim++;
if(this._anims[this._currAnim]){
this._anims[this._currAnim].play(null,true);
}
return this;
}});
dojo.lfx.combine=function(_3c0){
var _3c1=arguments;
if(dojo.lang.isArray(arguments[0])){
_3c1=arguments[0];
}
if(_3c1.length==1){
return _3c1[0];
}
return new dojo.lfx.Combine(_3c1);
};
dojo.lfx.chain=function(_3c2){
var _3c3=arguments;
if(dojo.lang.isArray(arguments[0])){
_3c3=arguments[0];
}
if(_3c3.length==1){
return _3c3[0];
}
return new dojo.lfx.Chain(_3c3);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _3c5;
do{
_3c5=dojo.html.getStyle(node,"background-color");
if(_3c5.toLowerCase()=="rgba(0, 0, 0, 0)"){
_3c5="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_3c5));
if(_3c5=="transparent"){
_3c5=[255,255,255,0];
}else{
_3c5=dojo.gfx.color.extractRGB(_3c5);
}
return _3c5;
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_3c6){
if(!_3c6){
return [];
}
if(dojo.lang.isArrayLike(_3c6)){
if(!_3c6.alreadyChecked){
var n=[];
dojo.lang.forEach(_3c6,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _3c6;
}
}else{
var n=[];
n.push(dojo.byId(_3c6));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_3c9,_3ca,_3cb,_3cc,_3cd){
_3c9=dojo.lfx.html._byId(_3c9);
var _3ce={"propertyMap":_3ca,"nodes":_3c9,"duration":_3cb,"easing":_3cc||dojo.lfx.easeDefault};
var _3cf=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _3d3 in pm){
pm[_3d3].property=_3d3;
parr.push(pm[_3d3]);
}
pm=args.propertyMap=parr;
}
dojo.lang.forEach(pm,function(prop){
if(dj_undef("start",prop)){
if(prop.property!="opacity"){
prop.start=parseInt(dojo.html.getComputedStyle(args.nodes[0],prop.property));
}else{
prop.start=dojo.html.getOpacity(args.nodes[0]);
}
}
});
}
};
var _3d5=function(_3d6){
var _3d7=[];
dojo.lang.forEach(_3d6,function(c){
_3d7.push(Math.round(c));
});
return _3d7;
};
var _3d9=function(n,_3db){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _3db){
try{
if(s=="opacity"){
dojo.html.setOpacity(n,_3db[s]);
}else{
n.style[s]=_3db[s];
}
}
catch(e){
dojo.debug(e);
}
}
};
var _3dd=function(_3de){
this._properties=_3de;
this.diffs=new Array(_3de.length);
dojo.lang.forEach(_3de,function(prop,i){
if(dojo.lang.isFunction(prop.start)){
prop.start=prop.start(prop,i);
}
if(dojo.lang.isFunction(prop.end)){
prop.end=prop.end(prop,i);
}
if(dojo.lang.isArray(prop.start)){
this.diffs[i]=null;
}else{
if(prop.start instanceof dojo.gfx.color.Color){
prop.startRgb=prop.start.toRgb();
prop.endRgb=prop.end.toRgb();
}else{
this.diffs[i]=prop.end-prop.start;
}
}
},this);
this.getValue=function(n){
var ret={};
dojo.lang.forEach(this._properties,function(prop,i){
var _3e5=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_3e5=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_3e5+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_3e5+=")";
}else{
_3e5=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_3e5;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_3cf(_3ce);
anim.curve=new _3dd(_3ce.propertyMap);
},onAnimate:function(_3e8){
dojo.lang.forEach(_3ce.nodes,function(node){
_3d9(node,_3e8);
});
}},_3ce.duration,null,_3ce.easing);
if(_3cd){
for(var x in _3cd){
if(dojo.lang.isFunction(_3cd[x])){
anim.connect(x,anim,_3cd[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_3eb){
var _3ec=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_3eb)){
dojo.lang.forEach(_3eb,_3ec);
}else{
_3ec(_3eb);
}
};
dojo.lfx.html.fade=function(_3ee,_3ef,_3f0,_3f1,_3f2){
_3ee=dojo.lfx.html._byId(_3ee);
var _3f3={property:"opacity"};
if(!dj_undef("start",_3ef)){
_3f3.start=_3ef.start;
}else{
_3f3.start=function(){
return dojo.html.getOpacity(_3ee[0]);
};
}
if(!dj_undef("end",_3ef)){
_3f3.end=_3ef.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_3ee,[_3f3],_3f0,_3f1);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_3ee);
});
if(_3f2){
anim.connect("onEnd",function(){
_3f2(_3ee,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_3f5,_3f6,_3f7,_3f8){
return dojo.lfx.html.fade(_3f5,{end:1},_3f6,_3f7,_3f8);
};
dojo.lfx.html.fadeOut=function(_3f9,_3fa,_3fb,_3fc){
return dojo.lfx.html.fade(_3f9,{end:0},_3fa,_3fb,_3fc);
};
dojo.lfx.html.fadeShow=function(_3fd,_3fe,_3ff,_400){
_3fd=dojo.lfx.html._byId(_3fd);
dojo.lang.forEach(_3fd,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_3fd,_3fe,_3ff,_400);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_3fd)){
dojo.lang.forEach(_3fd,dojo.html.show);
}else{
dojo.html.show(_3fd);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_403,_404,_405,_406){
var anim=dojo.lfx.html.fadeOut(_403,_404,_405,function(){
if(dojo.lang.isArrayLike(_403)){
dojo.lang.forEach(_403,dojo.html.hide);
}else{
dojo.html.hide(_403);
}
if(_406){
_406(_403,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_408,_409,_40a,_40b){
_408=dojo.lfx.html._byId(_408);
var _40c=[];
dojo.lang.forEach(_408,function(node){
var _40e={};
var _40f,_410,_411;
with(node.style){
_40f=top;
_410=left;
_411=position;
top="-9999px";
left="-9999px";
position="absolute";
display="";
}
var _412=dojo.html.getBorderBox(node).height;
with(node.style){
top=_40f;
left=_410;
position=_411;
display="none";
}
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:1,end:function(){
return _412;
}}},_409,_40a);
anim.connect("beforeBegin",function(){
_40e.overflow=node.style.overflow;
_40e.height=node.style.height;
with(node.style){
overflow="hidden";
_412="1px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_40e.overflow;
_412=_40e.height;
}
if(_40b){
_40b(node,anim);
}
});
_40c.push(anim);
});
return dojo.lfx.combine(_40c);
};
dojo.lfx.html.wipeOut=function(_414,_415,_416,_417){
_414=dojo.lfx.html._byId(_414);
var _418=[];
dojo.lang.forEach(_414,function(node){
var _41a={};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:1}},_415,_416,{"beforeBegin":function(){
_41a.overflow=node.style.overflow;
_41a.height=node.style.height;
with(node.style){
overflow="hidden";
}
dojo.html.show(node);
},"onEnd":function(){
dojo.html.hide(node);
with(node.style){
overflow=_41a.overflow;
height=_41a.height;
}
if(_417){
_417(node,anim);
}
}});
_418.push(anim);
});
return dojo.lfx.combine(_418);
};
dojo.lfx.html.slideTo=function(_41c,_41d,_41e,_41f,_420){
_41c=dojo.lfx.html._byId(_41c);
var _421=[];
var _422=dojo.html.getComputedStyle;
dojo.lang.forEach(_41c,function(node){
var top=null;
var left=null;
var init=(function(){
var _427=node;
return function(){
var pos=_422(_427,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_422(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_422(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_427,true);
dojo.html.setStyleAttributes(_427,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_41d.top||0)},"left":{start:left,end:(_41d.left||0)}},_41e,_41f,{"beforeBegin":init});
if(_420){
anim.connect("onEnd",function(){
_420(_41c,anim);
});
}
_421.push(anim);
});
return dojo.lfx.combine(_421);
};
dojo.lfx.html.slideBy=function(_42b,_42c,_42d,_42e,_42f){
_42b=dojo.lfx.html._byId(_42b);
var _430=[];
var _431=dojo.html.getComputedStyle;
dojo.lang.forEach(_42b,function(node){
var top=null;
var left=null;
var init=(function(){
var _436=node;
return function(){
var pos=_431(_436,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_431(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_431(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_436,true);
dojo.html.setStyleAttributes(_436,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_42c.top||0)},"left":{start:left,end:left+(_42c.left||0)}},_42d,_42e).connect("beforeBegin",init);
if(_42f){
anim.connect("onEnd",function(){
_42f(_42b,anim);
});
}
_430.push(anim);
});
return dojo.lfx.combine(_430);
};
dojo.lfx.html.explode=function(_43a,_43b,_43c,_43d,_43e){
var h=dojo.html;
_43a=dojo.byId(_43a);
_43b=dojo.byId(_43b);
var _440=h.toCoordinateObject(_43a,true);
var _441=document.createElement("div");
h.copyStyle(_441,_43b);
if(_43b.explodeClassName){
_441.className=_43b.explodeClassName;
}
with(_441.style){
position="absolute";
display="none";
var _442=h.getStyle(_43a,"background-color");
backgroundColor=_442?_442.toLowerCase():"transparent";
backgroundColor=(backgroundColor=="transparent")?"rgb(221, 221, 221)":backgroundColor;
}
dojo.body().appendChild(_441);
with(_43b.style){
visibility="hidden";
display="block";
}
var _443=h.toCoordinateObject(_43b,true);
with(_43b.style){
display="none";
visibility="visible";
}
var _444={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_444[type]={start:_440[type],end:_443[type]};
});
var anim=new dojo.lfx.propertyAnimation(_441,_444,_43c,_43d,{"beforeBegin":function(){
h.setDisplay(_441,"block");
},"onEnd":function(){
h.setDisplay(_43b,"block");
_441.parentNode.removeChild(_441);
}});
if(_43e){
anim.connect("onEnd",function(){
_43e(_43b,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_447,end,_449,_44a,_44b){
var h=dojo.html;
_447=dojo.byId(_447);
end=dojo.byId(end);
var _44d=dojo.html.toCoordinateObject(_447,true);
var _44e=dojo.html.toCoordinateObject(end,true);
var _44f=document.createElement("div");
dojo.html.copyStyle(_44f,_447);
if(_447.explodeClassName){
_44f.className=_447.explodeClassName;
}
dojo.html.setOpacity(_44f,0.3);
with(_44f.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_447,"background-color").toLowerCase();
}
dojo.body().appendChild(_44f);
var _450={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_450[type]={start:_44d[type],end:_44e[type]};
});
var anim=new dojo.lfx.propertyAnimation(_44f,_450,_449,_44a,{"beforeBegin":function(){
dojo.html.hide(_447);
dojo.html.show(_44f);
},"onEnd":function(){
_44f.parentNode.removeChild(_44f);
}});
if(_44b){
anim.connect("onEnd",function(){
_44b(_447,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_453,_454,_455,_456,_457){
_453=dojo.lfx.html._byId(_453);
var _458=[];
dojo.lang.forEach(_453,function(node){
var _45a=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _45c=dojo.html.getStyle(node,"background-image");
var _45d=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_45a.length>3){
_45a.pop();
}
var rgb=new dojo.gfx.color.Color(_454);
var _45f=new dojo.gfx.color.Color(_45a);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_45f}},_455,_456,{"beforeBegin":function(){
if(_45c){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_45c){
node.style.backgroundImage=_45c;
}
if(_45d){
node.style.backgroundColor="transparent";
}
if(_457){
_457(node,anim);
}
}});
_458.push(anim);
});
return dojo.lfx.combine(_458);
};
dojo.lfx.html.unhighlight=function(_461,_462,_463,_464,_465){
_461=dojo.lfx.html._byId(_461);
var _466=[];
dojo.lang.forEach(_461,function(node){
var _468=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_462);
var _46a=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_468,end:rgb}},_463,_464,{"beforeBegin":function(){
if(_46a){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_468.toRgb().join(",")+")";
},"onEnd":function(){
if(_465){
_465(node,anim);
}
}});
_466.push(anim);
});
return dojo.lfx.combine(_466);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.provide("dojo.lfx.*");
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_46d){
var _46e=window,_46f=2;
if(!dojo.lang.isFunction(func)){
_46e=func;
func=_46d;
_46d=arguments[2];
_46f++;
}
if(dojo.lang.isString(func)){
func=_46e[func];
}
var args=[];
for(var i=_46f;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_46e,args);
},_46d);
};
dojo.lang.clearTimeout=function(_472){
dojo.global().clearTimeout(_472);
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
dojo.lang.getObjPathValue=function(_47b,_47c,_47d){
with(dojo.parseObjPath(_47b,_47c,_47d)){
return dojo.evalProp(prop,obj,_47d);
}
};
dojo.lang.setObjPathValue=function(_47e,_47f,_480,_481){
dojo.deprecated("dojo.lang.setObjPathValue","use dojo.parseObjPath and the '=' operator","0.6");
if(arguments.length<4){
_481=true;
}
with(dojo.parseObjPath(_47e,_480,_481)){
if(obj&&(_481||(prop in obj))){
obj[prop]=_47f;
}
}
};
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_483){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false,maxCalls:-1};
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
var _486=dl.nameAnonFunc(args[2],ao.adviceObj,_483);
ao.adviceFunc=_486;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _486=dl.nameAnonFunc(args[0],ao.srcObj,_483);
ao.srcFunc=_486;
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
var _486=dl.nameAnonFunc(args[1],dj_global,_483);
ao.srcFunc=_486;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _486=dl.nameAnonFunc(args[3],dj_global,_483);
ao.adviceObj=dj_global;
ao.adviceFunc=_486;
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
ao.maxCalls=(!isNaN(parseInt(args[11])))?args[11]:-1;
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _486=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_483);
ao.aroundFunc=_486;
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
var _488={};
for(var x in ao){
_488[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_488.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_488));
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
var _490;
if((arguments.length==1)&&(typeof a1=="object")){
_490=a1;
}else{
_490={srcObj:a1,srcFunc:a2};
}
_490.adviceFunc=function(){
var _491=[];
for(var x=0;x<arguments.length;x++){
_491.push(arguments[x]);
}
dojo.debug("("+_490.srcObj+")."+_490.srcFunc,":",_491.join(", "));
};
this.kwConnect(_490);
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
this.connectRunOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.maxCalls=1;
return this.connect(ao);
};
this._kwConnectImpl=function(_499,_49a){
var fn=(_49a)?"disconnect":"connect";
if(typeof _499["srcFunc"]=="function"){
_499.srcObj=_499["srcObj"]||dj_global;
var _49c=dojo.lang.nameAnonFunc(_499.srcFunc,_499.srcObj,true);
_499.srcFunc=_49c;
}
if(typeof _499["adviceFunc"]=="function"){
_499.adviceObj=_499["adviceObj"]||dj_global;
var _49c=dojo.lang.nameAnonFunc(_499.adviceFunc,_499.adviceObj,true);
_499.adviceFunc=_49c;
}
_499.srcObj=_499["srcObj"]||dj_global;
_499.adviceObj=_499["adviceObj"]||_499["targetObj"]||dj_global;
_499.adviceFunc=_499["adviceFunc"]||_499["targetFunc"];
return dojo.event[fn](_499);
};
this.kwConnect=function(_49d){
return this._kwConnectImpl(_49d,false);
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
if(!ao.srcObj[ao.srcFunc]){
return null;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc,true);
mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
return mjp;
};
this.kwDisconnect=function(_4a0){
return this._kwConnectImpl(_4a0,true);
};
};
dojo.event.MethodInvocation=function(_4a1,obj,args){
this.jp_=_4a1;
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
dojo.event.MethodJoinPoint=function(obj,_4a9){
this.object=obj||dj_global;
this.methodname=_4a9;
this.methodfunc=this.object[_4a9];
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_4ab){
if(!obj){
obj=dj_global;
}
var ofn=obj[_4ab];
if(!ofn){
ofn=obj[_4ab]=function(){
};
if(!obj[_4ab]){
dojo.raise("Cannot set do-nothing method on that object "+_4ab);
}
}else{
if((typeof ofn!="function")&&(!dojo.lang.isFunction(ofn))&&(!dojo.lang.isAlien(ofn))){
return null;
}
}
var _4ad=_4ab+"$joinpoint";
var _4ae=_4ab+"$joinpoint$method";
var _4af=obj[_4ad];
if(!_4af){
var _4b0=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_4b0=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_4ad,_4ae,_4ab]);
}
}
var _4b1=ofn.length;
obj[_4ae]=ofn;
_4af=obj[_4ad]=new dojo.event.MethodJoinPoint(obj,_4ae);
if(!_4b0){
obj[_4ab]=function(){
return _4af.run.apply(_4af,arguments);
};
}else{
obj[_4ab]=function(){
var args=[];
if(!arguments.length){
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
if((x==0)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _4af.run.apply(_4af,args);
};
}
obj[_4ab].__preJoinArity=_4b1;
}
return _4af;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{squelch:false,unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _4b7=[];
for(var x=0;x<args.length;x++){
_4b7[x]=args[x];
}
var _4b9=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _4bb=marr[0]||dj_global;
var _4bc=marr[1];
if(!_4bb[_4bc]){
dojo.raise("function \""+_4bc+"\" does not exist on \""+_4bb+"\"");
}
var _4bd=marr[2]||dj_global;
var _4be=marr[3];
var msg=marr[6];
var _4c0=marr[7];
if(_4c0>-1){
if(_4c0==0){
return;
}
marr[7]--;
}
var _4c1;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _4bb[_4bc].apply(_4bb,to.args);
}};
to.args=_4b7;
var _4c3=parseInt(marr[4]);
var _4c4=((!isNaN(_4c3))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _4c7=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_4b9(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_4be){
_4bd[_4be].call(_4bd,to);
}else{
if((_4c4)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_4bb[_4bc].call(_4bb,to);
}else{
_4bb[_4bc].apply(_4bb,args);
}
},_4c3);
}else{
if(msg){
_4bb[_4bc].call(_4bb,to);
}else{
_4bb[_4bc].apply(_4bb,args);
}
}
}
};
var _4ca=function(){
if(this.squelch){
try{
return _4b9.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _4b9.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_4ca);
}
var _4cb;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_4cb=mi.proceed();
}else{
if(this.methodfunc){
_4cb=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.debug(e,"when calling",this.methodname,"on",this.object,"with arguments",args);
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_4ca);
}
return (this.methodfunc)?_4cb:null;
},getArr:function(kind){
var type="after";
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
type="before";
}else{
if(kind=="around"){
type="around";
}
}
if(!this[type]){
this[type]=[];
}
return this[type];
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"],args["maxCalls"]);
},addAdvice:function(_4d0,_4d1,_4d2,_4d3,_4d4,_4d5,once,_4d7,rate,_4d9,_4da){
var arr=this.getArr(_4d4);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_4d0,_4d1,_4d2,_4d3,_4d7,rate,_4d9,_4da];
if(once){
if(this.hasAdvice(_4d0,_4d1,_4d4,arr)>=0){
return;
}
}
if(_4d5=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_4dd,_4de,_4df,arr){
if(!arr){
arr=this.getArr(_4df);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _4de=="object")?(new String(_4de)).toString():_4de;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_4dd)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_4e5,_4e6,_4e7,once){
var arr=this.getArr(_4e7);
var ind=this.hasAdvice(_4e5,_4e6,_4e7,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_4e5,_4e6,_4e7,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_4eb){
if(!this.topics[_4eb]){
this.topics[_4eb]=new this.TopicImpl(_4eb);
}
return this.topics[_4eb];
};
this.registerPublisher=function(_4ec,obj,_4ee){
var _4ec=this.getTopic(_4ec);
_4ec.registerPublisher(obj,_4ee);
};
this.subscribe=function(_4ef,obj,_4f1){
var _4ef=this.getTopic(_4ef);
_4ef.subscribe(obj,_4f1);
};
this.unsubscribe=function(_4f2,obj,_4f4){
var _4f2=this.getTopic(_4f2);
_4f2.unsubscribe(obj,_4f4);
};
this.destroy=function(_4f5){
this.getTopic(_4f5).destroy();
delete this.topics[_4f5];
};
this.publishApply=function(_4f6,args){
var _4f6=this.getTopic(_4f6);
_4f6.sendMessage.apply(_4f6,args);
};
this.publish=function(_4f8,_4f9){
var _4f8=this.getTopic(_4f8);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_4f8.sendMessage.apply(_4f8,args);
};
};
dojo.event.topic.TopicImpl=function(_4fc){
this.topicName=_4fc;
this.subscribe=function(_4fd,_4fe){
var tf=_4fe||_4fd;
var to=(!_4fe)?dj_global:_4fd;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_501,_502){
var tf=(!_502)?_501:_502;
var to=(!_502)?null:_501;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_505){
this._getJoinPoint().squelch=_505;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_506,_507){
dojo.event.connect(_506,_507,this,"sendMessage");
};
this.sendMessage=function(_508){
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
this.clobber=function(_50b){
var na;
var tna;
if(_50b){
tna=_50b.all||_50b.getElementsByTagName("*");
na=[_50b];
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
var _50f={};
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
if(dojo.widget){
for(var name in dojo.widget._templateCache){
if(dojo.widget._templateCache[name].node){
dojo.dom.destroyNode(dojo.widget._templateCache[name].node);
dojo.widget._templateCache[name].node=null;
delete dojo.widget._templateCache[name].node;
}
}
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
var _514=0;
this.normalizedEventName=function(_515){
switch(_515){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _515;
break;
default:
return _515.toLowerCase();
break;
}
};
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
this.addClobberNodeAttrs=function(node,_519){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_519.length;x++){
node.__clobberAttrs__.push(_519[x]);
}
};
this.removeListener=function(node,_51c,fp,_51e){
if(!_51e){
var _51e=false;
}
_51c=dojo.event.browser.normalizedEventName(_51c);
if((_51c=="onkey")||(_51c=="key")){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_51e);
}
_51c="onkeypress";
}
if(_51c.substr(0,2)=="on"){
_51c=_51c.substr(2);
}
if(node.removeEventListener){
node.removeEventListener(_51c,fp,_51e);
}
};
this.addListener=function(node,_520,fp,_522,_523){
if(!node){
return;
}
if(!_522){
var _522=false;
}
_520=dojo.event.browser.normalizedEventName(_520);
if((_520=="onkey")||(_520=="key")){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_522,_523);
}
_520="onkeypress";
}
if(_520.substr(0,2)!="on"){
_520="on"+_520;
}
if(!_523){
var _524=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_522){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_524=fp;
}
if(node.addEventListener){
node.addEventListener(_520.substr(2),_524,_522);
return _524;
}else{
if(typeof node[_520]=="function"){
var _527=node[_520];
node[_520]=function(e){
_527(e);
return _524(e);
};
}else{
node[_520]=_524;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_520]);
}
return _524;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(obj)&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_52a,_52b){
if(typeof _52a!="function"){
dojo.raise("listener not a function: "+_52a);
}
dojo.event.browser.currentEvent.currentTarget=_52b;
return _52a.call(_52b,dojo.event.browser.currentEvent);
};
this._stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this._preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_52e){
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
var _530=evt.keyCode;
if(_530>=65&&_530<=90&&evt.shiftKey==false){
_530+=32;
}
if(_530>=1&&_530<=26&&evt.ctrlKey){
_530+=96;
}
evt.key=String.fromCharCode(_530);
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
var _530=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_530+=32;
}
evt.key=String.fromCharCode(_530);
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
case 25:
evt.key=evt.KEY_TAB;
evt.shift=true;
break;
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
case 63236:
evt.key=evt.KEY_F1;
break;
case 63237:
evt.key=evt.KEY_F2;
break;
case 63238:
evt.key=evt.KEY_F3;
break;
case 63239:
evt.key=evt.KEY_F4;
break;
case 63240:
evt.key=evt.KEY_F5;
break;
case 63241:
evt.key=evt.KEY_F6;
break;
case 63242:
evt.key=evt.KEY_F7;
break;
case 63243:
evt.key=evt.KEY_F8;
break;
case 63244:
evt.key=evt.KEY_F9;
break;
case 63245:
evt.key=evt.KEY_F10;
break;
case 63246:
evt.key=evt.KEY_F11;
break;
case 63247:
evt.key=evt.KEY_F12;
break;
case 63250:
evt.key=evt.KEY_PAUSE;
break;
case 63272:
evt.key=evt.KEY_DELETE;
break;
case 63273:
evt.key=evt.KEY_HOME;
break;
case 63275:
evt.key=evt.KEY_END;
break;
case 63276:
evt.key=evt.KEY_PAGE_UP;
break;
case 63277:
evt.key=evt.KEY_PAGE_DOWN;
break;
case 63302:
evt.key=evt.KEY_INSERT;
break;
case 63248:
case 63249:
case 63289:
break;
default:
evt.key=evt.charCode>=evt.KEY_SPACE?String.fromCharCode(evt.charCode):evt.keyCode;
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
evt.currentTarget=(_52e?_52e:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _532=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_532.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_532.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this._stopPropagation;
evt.preventDefault=this._preventDefault;
}
return evt;
};
this.stopEvent=function(evt){
if(window.event){
evt.cancelBubble=true;
evt.returnValue=false;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.provide("dojo.event.*");
dojo.provide("dojo.lang.declare");
dojo.lang.declare=function(_534,_535,init,_537){
if((dojo.lang.isFunction(_537))||((!_537)&&(!dojo.lang.isFunction(init)))){
if(dojo.lang.isFunction(_537)){
dojo.deprecated("dojo.lang.declare("+_534+"...):","use class, superclass, initializer, properties argument order","0.6");
}
var temp=_537;
_537=init;
init=temp;
}
if(_537&&_537.initializer){
dojo.deprecated("dojo.lang.declare("+_534+"...):","specify initializer as third argument, not as an element in properties","0.6");
}
var _539=[];
if(dojo.lang.isArray(_535)){
_539=_535;
_535=_539.shift();
}
if(!init){
init=dojo.evalObjPath(_534,false);
if((init)&&(!dojo.lang.isFunction(init))){
init=null;
}
}
var ctor=dojo.lang.declare._makeConstructor();
var scp=(_535?_535.prototype:null);
if(scp){
scp.prototyping=true;
ctor.prototype=new _535();
scp.prototyping=false;
}
ctor.superclass=scp;
ctor.mixins=_539;
for(var i=0,l=_539.length;i<l;i++){
dojo.lang.extend(ctor,_539[i].prototype);
}
ctor.prototype.initializer=null;
ctor.prototype.declaredClass=_534;
if(dojo.lang.isArray(_537)){
dojo.lang.extend.apply(dojo.lang,[ctor].concat(_537));
}else{
dojo.lang.extend(ctor,(_537)||{});
}
dojo.lang.extend(ctor,dojo.lang.declare._common);
ctor.prototype.constructor=ctor;
ctor.prototype.initializer=(ctor.prototype.initializer)||(init)||(function(){
});
var _53e=dojo.parseObjPath(_534,null,true);
_53e.obj[_53e.prop]=ctor;
return ctor;
};
dojo.lang.declare._makeConstructor=function(){
return function(){
var self=this._getPropContext();
var s=self.constructor.superclass;
if((s)&&(s.constructor)){
if(s.constructor==arguments.callee){
this._inherited("constructor",arguments);
}else{
this._contextMethod(s,"constructor",arguments);
}
}
var ms=(self.constructor.mixins)||([]);
for(var i=0,m;(m=ms[i]);i++){
(((m.prototype)&&(m.prototype.initializer))||(m)).apply(this,arguments);
}
if((!this.prototyping)&&(self.initializer)){
self.initializer.apply(this,arguments);
}
};
};
dojo.lang.declare._common={_getPropContext:function(){
return (this.___proto||this);
},_contextMethod:function(_544,_545,args){
var _547,_548=this.___proto;
this.___proto=_544;
try{
_547=_544[_545].apply(this,(args||[]));
}
catch(e){
throw e;
}
finally{
this.___proto=_548;
}
return _547;
},_inherited:function(prop,args){
var p=this._getPropContext();
do{
if((!p.constructor)||(!p.constructor.superclass)){
return;
}
p=p.constructor.superclass;
}while(!(prop in p));
return (dojo.lang.isFunction(p[prop])?this._contextMethod(p,prop,args):p[prop]);
}};
dojo.declare=dojo.lang.declare;
dojo.provide("dojo.logging.Logger");
dojo.provide("dojo.logging.LogFilter");
dojo.provide("dojo.logging.Record");
dojo.provide("dojo.log");
dojo.logging.Record=function(_54c,_54d){
this.level=_54c;
this.message="";
this.msgArgs=[];
this.time=new Date();
if(dojo.lang.isArray(_54d)){
if(_54d.length>0&&dojo.lang.isString(_54d[0])){
this.message=_54d.shift();
}
this.msgArgs=_54d;
}else{
this.message=_54d;
}
};
dojo.logging.LogFilter=function(_54e){
this.passChain=_54e||"";
this.filter=function(_54f){
return true;
};
};
dojo.logging.Logger=function(){
this.cutOffLevel=0;
this.propagate=true;
this.parent=null;
this.data=[];
this.filters=[];
this.handlers=[];
};
dojo.extend(dojo.logging.Logger,{_argsToArr:function(args){
var ret=[];
for(var x=0;x<args.length;x++){
ret.push(args[x]);
}
return ret;
},setLevel:function(lvl){
this.cutOffLevel=parseInt(lvl);
},isEnabledFor:function(lvl){
return parseInt(lvl)>=this.cutOffLevel;
},getEffectiveLevel:function(){
if((this.cutOffLevel==0)&&(this.parent)){
return this.parent.getEffectiveLevel();
}
return this.cutOffLevel;
},addFilter:function(flt){
this.filters.push(flt);
return this.filters.length-1;
},removeFilterByIndex:function(_556){
if(this.filters[_556]){
delete this.filters[_556];
return true;
}
return false;
},removeFilter:function(_557){
for(var x=0;x<this.filters.length;x++){
if(this.filters[x]===_557){
delete this.filters[x];
return true;
}
}
return false;
},removeAllFilters:function(){
this.filters=[];
},filter:function(rec){
for(var x=0;x<this.filters.length;x++){
if((this.filters[x]["filter"])&&(!this.filters[x].filter(rec))||(rec.level<this.cutOffLevel)){
return false;
}
}
return true;
},addHandler:function(hdlr){
this.handlers.push(hdlr);
return this.handlers.length-1;
},handle:function(rec){
if((!this.filter(rec))||(rec.level<this.cutOffLevel)){
return false;
}
for(var x=0;x<this.handlers.length;x++){
if(this.handlers[x]["handle"]){
this.handlers[x].handle(rec);
}
}
return true;
},log:function(lvl,msg){
if((this.propagate)&&(this.parent)&&(this.parent.rec.level>=this.cutOffLevel)){
this.parent.log(lvl,msg);
return false;
}
this.handle(new dojo.logging.Record(lvl,msg));
return true;
},debug:function(msg){
return this.logType("DEBUG",this._argsToArr(arguments));
},info:function(msg){
return this.logType("INFO",this._argsToArr(arguments));
},warning:function(msg){
return this.logType("WARNING",this._argsToArr(arguments));
},error:function(msg){
return this.logType("ERROR",this._argsToArr(arguments));
},critical:function(msg){
return this.logType("CRITICAL",this._argsToArr(arguments));
},exception:function(msg,e,_567){
if(e){
var _568=[e.name,(e.description||e.message)];
if(e.fileName){
_568.push(e.fileName);
_568.push("line "+e.lineNumber);
}
msg+=" "+_568.join(" : ");
}
this.logType("ERROR",msg);
if(!_567){
throw e;
}
},logType:function(type,args){
return this.log.apply(this,[dojo.logging.log.getLevel(type),args]);
},warn:function(){
this.warning.apply(this,arguments);
},err:function(){
this.error.apply(this,arguments);
},crit:function(){
this.critical.apply(this,arguments);
}});
dojo.logging.LogHandler=function(_56b){
this.cutOffLevel=(_56b)?_56b:0;
this.formatter=null;
this.data=[];
this.filters=[];
};
dojo.lang.extend(dojo.logging.LogHandler,{setFormatter:function(_56c){
dojo.unimplemented("setFormatter");
},flush:function(){
},close:function(){
},handleError:function(){
dojo.deprecated("dojo.logging.LogHandler.handleError","use handle()","0.6");
},handle:function(_56d){
if((this.filter(_56d))&&(_56d.level>=this.cutOffLevel)){
this.emit(_56d);
}
},emit:function(_56e){
dojo.unimplemented("emit");
}});
void (function(){
var _56f=["setLevel","addFilter","removeFilterByIndex","removeFilter","removeAllFilters","filter"];
var tgt=dojo.logging.LogHandler.prototype;
var src=dojo.logging.Logger.prototype;
for(var x=0;x<_56f.length;x++){
tgt[_56f[x]]=src[_56f[x]];
}
})();
dojo.logging.log=new dojo.logging.Logger();
dojo.logging.log.levels=[{"name":"DEBUG","level":1},{"name":"INFO","level":2},{"name":"WARNING","level":3},{"name":"ERROR","level":4},{"name":"CRITICAL","level":5}];
dojo.logging.log.loggers={};
dojo.logging.log.getLogger=function(name){
if(!this.loggers[name]){
this.loggers[name]=new dojo.logging.Logger();
this.loggers[name].parent=this;
}
return this.loggers[name];
};
dojo.logging.log.getLevelName=function(lvl){
for(var x=0;x<this.levels.length;x++){
if(this.levels[x].level==lvl){
return this.levels[x].name;
}
}
return null;
};
dojo.logging.log.getLevel=function(name){
for(var x=0;x<this.levels.length;x++){
if(this.levels[x].name.toUpperCase()==name.toUpperCase()){
return this.levels[x].level;
}
}
return null;
};
dojo.declare("dojo.logging.MemoryLogHandler",dojo.logging.LogHandler,function(_578,_579,_57a,_57b){
dojo.logging.LogHandler.call(this,_578);
this.numRecords=(typeof djConfig["loggingNumRecords"]!="undefined")?djConfig["loggingNumRecords"]:((_579)?_579:-1);
this.postType=(typeof djConfig["loggingPostType"]!="undefined")?djConfig["loggingPostType"]:(_57a||-1);
this.postInterval=(typeof djConfig["loggingPostInterval"]!="undefined")?djConfig["loggingPostInterval"]:(_57a||-1);
},{emit:function(_57c){
if(!djConfig.isDebug){
return;
}
var _57d=String(dojo.log.getLevelName(_57c.level)+": "+_57c.time.toLocaleTimeString())+": "+_57c.message;
if(!dj_undef("println",dojo.hostenv)){
dojo.hostenv.println(_57d,_57c.msgArgs);
}
this.data.push(_57c);
if(this.numRecords!=-1){
while(this.data.length>this.numRecords){
this.data.shift();
}
}
}});
dojo.logging.logQueueHandler=new dojo.logging.MemoryLogHandler(0,50,0,10000);
dojo.logging.log.addHandler(dojo.logging.logQueueHandler);
dojo.log=dojo.logging.log;
dojo.provide("dojo.logging.*");
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
dojo.string.repeat=function(str,_584,_585){
var out="";
for(var i=0;i<_584;i++){
out+=str;
if(_585&&i<_584-1){
out+=_585;
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
dojo.io.Request=function(url,_594,_595,_596){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_594){
this.mimetype=_594;
}
if(_595){
this.transport=_595;
}
if(arguments.length>=4){
this.changeUrl=_596;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,load:function(type,data,_599,_59a){
},error:function(type,_59c,_59d,_59e){
},timeout:function(type,_5a0,_5a1,_5a2){
},handle:function(type,data,_5a5,_5a6){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_5a7){
if(_5a7["url"]){
_5a7.url=_5a7.url.toString();
}
if(_5a7["formNode"]){
_5a7.formNode=dojo.byId(_5a7.formNode);
}
if(!_5a7["method"]&&_5a7["formNode"]&&_5a7["formNode"].method){
_5a7.method=_5a7["formNode"].method;
}
if(!_5a7["handle"]&&_5a7["handler"]){
_5a7.handle=_5a7.handler;
}
if(!_5a7["load"]&&_5a7["loaded"]){
_5a7.load=_5a7.loaded;
}
if(!_5a7["changeUrl"]&&_5a7["changeURL"]){
_5a7.changeUrl=_5a7.changeURL;
}
_5a7.encoding=dojo.lang.firstValued(_5a7["encoding"],djConfig["bindEncoding"],"");
_5a7.sendTransport=dojo.lang.firstValued(_5a7["sendTransport"],djConfig["ioSendTransport"],false);
var _5a8=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_5a7[fn]&&_5a8(_5a7[fn])){
continue;
}
if(_5a7["handle"]&&_5a8(_5a7["handle"])){
_5a7[fn]=_5a7.handle;
}
}
dojo.lang.mixin(this,_5a7);
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
dojo.io.bind=function(_5af){
if(!(_5af instanceof dojo.io.Request)){
try{
_5af=new dojo.io.Request(_5af);
}
catch(e){
dojo.debug(e);
}
}
var _5b0="";
if(_5af["transport"]){
_5b0=_5af["transport"];
if(!this[_5b0]){
dojo.io.sendBindError(_5af,"No dojo.io.bind() transport with name '"+_5af["transport"]+"'.");
return _5af;
}
if(!this[_5b0].canHandle(_5af)){
dojo.io.sendBindError(_5af,"dojo.io.bind() transport with name '"+_5af["transport"]+"' cannot handle this type of request.");
return _5af;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_5af))){
_5b0=tmp;
break;
}
}
if(_5b0==""){
dojo.io.sendBindError(_5af,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _5af;
}
}
this[_5b0].bind(_5af);
_5af.bindSuccess=true;
return _5af;
};
dojo.io.sendBindError=function(_5b3,_5b4){
if((typeof _5b3.error=="function"||typeof _5b3.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _5b5=new dojo.io.Error(_5b4);
setTimeout(function(){
_5b3[(typeof _5b3.error=="function")?"error":"handle"]("error",_5b5,null,_5b3);
},50);
}else{
dojo.raise(_5b4);
}
};
dojo.io.queueBind=function(_5b6){
if(!(_5b6 instanceof dojo.io.Request)){
try{
_5b6=new dojo.io.Request(_5b6);
}
catch(e){
dojo.debug(e);
}
}
var _5b7=_5b6.load;
_5b6.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_5b7.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _5b9=_5b6.error;
_5b6.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_5b9.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_5b6);
dojo.io._dispatchNextQueueBind();
return _5b6;
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
dojo.io.argsFromMap=function(map,_5bc,last){
var enc=/utf/i.test(_5bc||"")?encodeURIComponent:dojo.string.encodeAscii;
var _5bf=[];
var _5c0=new Object();
for(var name in map){
var _5c2=function(elt){
var val=enc(name)+"="+enc(elt);
_5bf[(last==name)?"push":"unshift"](val);
};
if(!_5c0[name]){
var _5c5=map[name];
if(dojo.lang.isArray(_5c5)){
dojo.lang.forEach(_5c5,_5c2);
}else{
_5c2(_5c5);
}
}
}
return _5bf.join("&");
};
dojo.io.setIFrameSrc=function(_5c6,src,_5c8){
try{
var r=dojo.render.html;
if(!_5c8){
if(r.safari){
_5c6.location=src;
}else{
frames[_5c6.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_5c6.contentWindow.document;
}else{
if(r.safari){
idoc=_5c6.document;
}else{
idoc=_5c6.contentWindow;
}
}
if(!idoc){
_5c6.location=src;
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
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_5cb,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _5cb.replace(/\%\{(\w+)\}/g,function(_5ce,key){
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
var _5d1=str.split(" ");
for(var i=0;i<_5d1.length;i++){
_5d1[i]=_5d1[i].charAt(0).toUpperCase()+_5d1[i].substring(1);
}
return _5d1.join(" ");
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
var _5d6=escape(str);
var _5d7,re=/%u([0-9A-F]{4})/i;
while((_5d7=_5d6.match(re))){
var num=Number("0x"+_5d7[1]);
var _5da=escape("&#"+num+";");
ret+=_5d6.substring(0,_5d7.index)+_5da;
_5d6=_5d6.substring(_5d7.index+_5d7[0].length);
}
ret+=_5d6.replace(/\+/g,"%2B");
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
dojo.string.escapeXml=function(str,_5df){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_5df){
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
dojo.string.endsWith=function(str,end,_5e8){
if(_5e8){
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
dojo.string.startsWith=function(str,_5ec,_5ed){
if(_5ed){
str=str.toLowerCase();
_5ec=_5ec.toLowerCase();
}
return str.indexOf(_5ec)==0;
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
dojo.string.normalizeNewlines=function(text,_5f3){
if(_5f3=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_5f3=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_5f5){
var _5f6=[];
for(var i=0,_5f8=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_5f5){
_5f6.push(str.substring(_5f8,i));
_5f8=i+1;
}
}
_5f6.push(str.substr(_5f8));
return _5f6;
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
dojo.undo.browser={initialHref:(!dj_undef("window"))?window.location.href:"",initialHash:(!dj_undef("window"))?window.location.hash:"",moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
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
var _5fd=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_5ff){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_5fd.apply(this,[_5ff]);
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
var _600=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_602){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_600){
_600.apply(this,[_602]);
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
},iframeLoaded:function(evt,_605){
if(!dojo.render.html.opera){
var _606=this._getUrlQuery(_605.href);
if(_606==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_606==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_606==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _607=this.historyStack.pop();
if(!_607){
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
this.forwardStack.push(_607);
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
var _60e=url.split("?");
if(_60e.length<2){
return null;
}else{
return _60e[1];
}
},_loadIframeHistory:function(){
var url=dojo.hostenv.getBaseScriptUri()+"iframe_history.html?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
if(!dj_undef("window")){
dojo.io.checkChildrenForFile=function(node){
var _611=false;
var _612=node.getElementsByTagName("input");
dojo.lang.forEach(_612,function(_613){
if(_611){
return;
}
if(_613.getAttribute("type")=="file"){
_611=true;
}
});
return _611;
};
dojo.io.formHasFile=function(_614){
return dojo.io.checkChildrenForFile(_614);
};
dojo.io.updateNode=function(node,_616){
node=dojo.byId(node);
var args=_616;
if(dojo.lang.isString(_616)){
args={url:_616};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
dojo.dom.destroyNode(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_61d,_61e,_61f){
if((!_61d)||(!_61d.tagName)||(!_61d.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_61f){
_61f=dojo.io.formFilter;
}
var enc=/utf/i.test(_61e||"")?encodeURIComponent:dojo.string.encodeAscii;
var _621=[];
for(var i=0;i<_61d.elements.length;i++){
var elm=_61d.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_61f(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_621.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_621.push(name+"="+enc(elm.value));
}
}else{
_621.push(name+"="+enc(elm.value));
}
}
}
var _627=_61d.getElementsByTagName("input");
for(var i=0;i<_627.length;i++){
var _628=_627[i];
if(_628.type.toLowerCase()=="image"&&_628.form==_61d&&_61f(_628)){
var name=enc(_628.name);
_621.push(name+"="+enc(_628.value));
_621.push(name+".x=0");
_621.push(name+".y=0");
}
}
return _621.join("&")+"&";
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
var _62e=form.getElementsByTagName("input");
for(var i=0;i<_62e.length;i++){
var _62f=_62e[i];
if(_62f.type.toLowerCase()=="image"&&_62f.form==form){
this.connect(_62f,"onclick","click");
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
var _636=false;
if(node.disabled||!node.name){
_636=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_636=node==this.clickedButton;
}else{
_636=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _636;
},connect:function(_637,_638,_639){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_637,_638,this,_639);
}else{
var fcn=dojo.lang.hitch(this,_639);
_637[_638]=function(e){
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
var _63c=this;
var _63d={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_63f,_640){
return url+"|"+_63f+"|"+_640.toLowerCase();
}
function addToCache(url,_642,_643,http){
_63d[getCacheKey(url,_642,_643)]=http;
}
function getFromCache(url,_646,_647){
return _63d[getCacheKey(url,_646,_647)];
}
this.clearCache=function(){
_63d={};
};
function doLoad(_648,http,url,_64b,_64c){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_648.method.toLowerCase()=="head"){
var _64e=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _64e;
};
var _64f=_64e.split(/[\r\n]+/g);
for(var i=0;i<_64f.length;i++){
var pair=_64f[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_648.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_648.mimetype=="text/json"||_648.mimetype=="application/json"){
try{
ret=dj_eval("("+http.responseText+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_648.mimetype=="application/xml")||(_648.mimetype=="text/xml")){
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
if(_64c){
addToCache(url,_64b,_648.method,http);
}
_648[(typeof _648.load=="function")?"load":"handle"]("load",ret,http,_648);
}else{
var _652=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_648[(typeof _648.error=="function")?"error":"handle"]("error",_652,http,_648);
}
}
function setHeaders(http,_654){
if(_654["headers"]){
for(var _655 in _654["headers"]){
if(_655.toLowerCase()=="content-type"&&!_654["contentType"]){
_654["contentType"]=_654["headers"][_655];
}else{
http.setRequestHeader(_655,_654["headers"][_655]);
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
if(!dojo.hostenv._blockAsync&&!_63c._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
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
catch(e){
try{
var _659=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_659,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
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
var _65a=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_65b){
return _65a&&dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript","text/json","application/json"],(_65b["mimetype"].toLowerCase()||""))&&!(_65b["formNode"]&&dojo.io.formHasFile(_65b["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_65c){
var url=_65c.url;
var _65e="";
if(_65c["formNode"]){
var ta=_65c.formNode.getAttribute("action");
if((ta)&&(!_65c["url"])){
url=ta;
}
var tp=_65c.formNode.getAttribute("method");
if((tp)&&(!_65c["method"])){
_65c.method=tp;
}
_65e+=dojo.io.encodeForm(_65c.formNode,_65c.encoding,_65c["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_65c["file"]){
_65c.method="post";
}
if(!_65c["method"]){
_65c.method="get";
}
if(_65c.method.toLowerCase()=="get"){
_65c.multipart=false;
}else{
if(_65c["file"]){
_65c.multipart=true;
}else{
if(!_65c["multipart"]){
_65c.multipart=false;
}
}
}
if(_65c["backButton"]||_65c["back"]||_65c["changeUrl"]){
dojo.undo.browser.addToHistory(_65c);
}
var _661=_65c["content"]||{};
if(_65c.sendTransport){
_661["dojo.transport"]="xmlhttp";
}
do{
if(_65c.postContent){
_65e=_65c.postContent;
break;
}
if(_661){
_65e+=dojo.io.argsFromMap(_661,_65c.encoding);
}
if(_65c.method.toLowerCase()=="get"||!_65c.multipart){
break;
}
var t=[];
if(_65e.length){
var q=_65e.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_65c.file){
if(dojo.lang.isArray(_65c.file)){
for(var i=0;i<_65c.file.length;++i){
var o=_65c.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_65c.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_65e=t.join("\r\n");
}
}while(false);
var _667=_65c["sync"]?false:true;
var _668=_65c["preventCache"]||(this.preventCache==true&&_65c["preventCache"]!=false);
var _669=_65c["useCache"]==true||(this.useCache==true&&_65c["useCache"]!=false);
if(!_668&&_669){
var _66a=getFromCache(url,_65e,_65c.method);
if(_66a){
doLoad(_65c,_66a,url,_65e,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_65c);
var _66c=false;
if(_667){
var _66d=this.inFlight.push({"req":_65c,"http":http,"url":url,"query":_65e,"useCache":_669,"startTime":_65c.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_63c._blockAsync=true;
}
if(_65c.method.toLowerCase()=="post"){
if(!_65c.user){
http.open("POST",url,_667);
}else{
http.open("POST",url,_667,_65c.user,_65c.password);
}
setHeaders(http,_65c);
http.setRequestHeader("Content-Type",_65c.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_65c.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_65e);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_65c,{status:404},url,_65e,_669);
}
}else{
var _66e=url;
if(_65e!=""){
_66e+=(_66e.indexOf("?")>-1?"&":"?")+_65e;
}
if(_668){
_66e+=(dojo.string.endsWithAny(_66e,"?","&")?"":(_66e.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_65c.user){
http.open(_65c.method.toUpperCase(),_66e,_667);
}else{
http.open(_65c.method.toUpperCase(),_66e,_667,_65c.user,_65c.password);
}
setHeaders(http,_65c);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_65c,{status:404},url,_65e,_669);
}
}
if(!_667){
doLoad(_65c,http,url,_65e,_669);
_63c._blockAsync=false;
}
_65c.abort=function(){
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
}
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_670,days,path,_673,_674){
var _675=-1;
if((typeof days=="number")&&(days>=0)){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_675=d.toGMTString();
}
_670=escape(_670);
document.cookie=name+"="+_670+";"+(_675!=-1?" expires="+_675+";":"")+(path?"path="+path:"")+(_673?"; domain="+_673:"")+(_674?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _679=document.cookie.substring(idx+name.length+1);
var end=_679.indexOf(";");
if(end==-1){
end=_679.length;
}
_679=_679.substring(0,end);
_679=unescape(_679);
return _679;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_680,_681,_682){
if(arguments.length==5){
_682=_680;
_680=null;
_681=null;
}
var _683=[],_684,_685="";
if(!_682){
_684=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_684){
_684={};
}
for(var prop in obj){
if(obj[prop]==null){
delete _684[prop];
}else{
if((typeof obj[prop]=="string")||(typeof obj[prop]=="number")){
_684[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _684){
_683.push(escape(prop)+"="+escape(_684[prop]));
}
_685=_683.join("&");
}
dojo.io.cookie.setCookie(name,_685,days,path,_680,_681);
};
dojo.io.cookie.getObjectCookie=function(name){
var _688=null,_689=dojo.io.cookie.getCookie(name);
if(_689){
_688={};
var _68a=_689.split("&");
for(var i=0;i<_68a.length;i++){
var pair=_68a[i].split("=");
var _68d=pair[1];
if(isNaN(_68d)){
_68d=unescape(pair[1]);
}
_688[unescape(pair[0])]=_68d;
}
}
return _688;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _68e=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_68e=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.provide("dojo.io.*");
dojo.provide("dojo.uri.*");
dojo.provide("dojo.io.IframeIO");
dojo.io.createIFrame=function(_68f,_690,uri){
if(window[_68f]){
return window[_68f];
}
if(window.frames[_68f]){
return window.frames[_68f];
}
var r=dojo.render.html;
var _693=null;
var turi=uri||dojo.uri.dojoUri("iframe_history.html?noInit=true");
var _695=((r.ie)&&(dojo.render.os.win))?"<iframe name=\""+_68f+"\" src=\""+turi+"\" onload=\""+_690+"\">":"iframe";
_693=document.createElement(_695);
with(_693){
name=_68f;
setAttribute("name",_68f);
id=_68f;
}
dojo.body().appendChild(_693);
window[_68f]=_693;
with(_693.style){
if(!r.safari){
position="absolute";
}
left=top="0px";
height=width="1px";
visibility="hidden";
}
if(!r.ie){
dojo.io.setIFrameSrc(_693,turi,true);
_693.onload=new Function(_690);
}
return _693;
};
dojo.io.IframeTransport=new function(){
var _696=this;
this.currentRequest=null;
this.requestQueue=[];
this.iframeName="dojoIoIframe";
this.fireNextRequest=function(){
try{
if((this.currentRequest)||(this.requestQueue.length==0)){
return;
}
var cr=this.currentRequest=this.requestQueue.shift();
cr._contentToClean=[];
var fn=cr["formNode"];
var _699=cr["content"]||{};
if(cr.sendTransport){
_699["dojo.transport"]="iframe";
}
if(fn){
if(_699){
for(var x in _699){
if(!fn[x]){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_699[x]+"'>");
fn.appendChild(tn);
}else{
tn=document.createElement("input");
fn.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_699[x];
}
cr._contentToClean.push(x);
}else{
fn[x].value=_699[x];
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
var _69c=dojo.io.argsFromMap(this.currentRequest.content);
var _69d=cr.url+(cr.url.indexOf("?")>-1?"&":"?")+_69c;
dojo.io.setIFrameSrc(this.iframe,_69d,true);
}
}
catch(e){
this.iframeOnload(e);
}
};
this.canHandle=function(_69e){
return ((dojo.lang.inArray(["text/plain","text/html","text/javascript","text/json","application/json"],_69e["mimetype"]))&&(dojo.lang.inArray(["post","get"],_69e["method"].toLowerCase()))&&(!((_69e["sync"])&&(_69e["sync"]==true))));
};
this.bind=function(_69f){
if(!this["iframe"]){
this.setUpIframe();
}
this.requestQueue.push(_69f);
this.fireNextRequest();
return;
};
this.setUpIframe=function(){
this.iframe=dojo.io.createIFrame(this.iframeName,"dojo.io.IframeTransport.iframeOnload();");
};
this.iframeOnload=function(_6a0){
if(!_696.currentRequest){
_696.fireNextRequest();
return;
}
var req=_696.currentRequest;
if(req.formNode){
var _6a2=req._contentToClean;
for(var i=0;i<_6a2.length;i++){
var key=_6a2[i];
if(dojo.render.html.safari){
var _6a5=req.formNode;
for(var j=0;j<_6a5.childNodes.length;j++){
var _6a7=_6a5.childNodes[j];
if(_6a7.name==key){
var _6a8=_6a7.parentNode;
_6a8.removeChild(_6a7);
break;
}
}
}else{
var _6a9=req.formNode[key];
req.formNode.removeChild(_6a9);
req.formNode[key]=null;
}
}
if(req["_originalAction"]){
req.formNode.setAttribute("action",req._originalAction);
}
if(req["_originalTarget"]){
req.formNode.setAttribute("target",req._originalTarget);
req.formNode.target=req._originalTarget;
}
}
var _6aa=function(_6ab){
var doc=_6ab.contentDocument||((_6ab.contentWindow)&&(_6ab.contentWindow.document))||((_6ab.name)&&(document.frames[_6ab.name])&&(document.frames[_6ab.name].document))||null;
return doc;
};
var _6ad;
var _6ae=false;
if(_6a0){
this._callError(req,"IframeTransport Request Error: "+_6a0);
}else{
var ifd=_6aa(_696.iframe);
try{
var cmt=req.mimetype;
if((cmt=="text/javascript")||(cmt=="text/json")||(cmt=="application/json")){
var js=ifd.getElementsByTagName("textarea")[0].value;
if(cmt=="text/json"||cmt=="application/json"){
js="("+js+")";
}
_6ad=dj_eval(js);
}else{
if(cmt=="text/html"){
_6ad=ifd;
}else{
_6ad=ifd.getElementsByTagName("textarea")[0].value;
}
}
_6ae=true;
}
catch(e){
this._callError(req,"IframeTransport Error: "+e);
}
}
try{
if(_6ae&&dojo.lang.isFunction(req["load"])){
req.load("load",_6ad,req);
}
}
catch(e){
throw e;
}
finally{
_696.currentRequest=null;
_696.fireNextRequest();
}
};
this._callError=function(req,_6b3){
var _6b4=new dojo.io.Error(_6b3);
if(dojo.lang.isFunction(req["error"])){
req.error("error",_6b4,req);
}
};
dojo.io.transports.addTransport("IframeTransport");
};
dojo.provide("dojo.string.Builder");
dojo.string.Builder=function(str){
this.arrConcat=(dojo.render.html.capable&&dojo.render.html["ie"]);
var a=[];
var b="";
var _6b8=this.length=b.length;
if(this.arrConcat){
if(b.length>0){
a.push(b);
}
b="";
}
this.toString=this.valueOf=function(){
return (this.arrConcat)?a.join(""):b;
};
this.append=function(){
for(var x=0;x<arguments.length;x++){
var s=arguments[x];
if(dojo.lang.isArrayLike(s)){
this.append.apply(this,s);
}else{
if(this.arrConcat){
a.push(s);
}else{
b+=s;
}
_6b8+=s.length;
this.length=_6b8;
}
}
return this;
};
this.concat=function(){
return this.append.apply(this,arguments);
};
this.clear=function(){
a=[];
b="";
_6b8=this.length=0;
return this;
};
this.remove=function(f,l){
var s="";
if(this.arrConcat){
b=a.join("");
}
a=[];
if(f>0){
s=b.substring(0,(f-1));
}
b=s+b.substring(f+l);
_6b8=this.length=b.length;
if(this.arrConcat){
a.push(b);
b="";
}
return this;
};
this.replace=function(o,n){
if(this.arrConcat){
b=a.join("");
}
a=[];
b=b.replace(o,n);
_6b8=this.length=b.length;
if(this.arrConcat){
a.push(b);
b="";
}
return this;
};
this.insert=function(idx,s){
if(this.arrConcat){
b=a.join("");
}
a=[];
if(idx==0){
b=s+b;
}else{
var t=b.split("");
t.splice(idx,0,s);
b=t.join("");
}
_6b8=this.length=b.length;
if(this.arrConcat){
a.push(b);
b="";
}
return this;
};
this.append.apply(this,arguments);
};
dojo.provide("dojo.string.*");
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_6c3){
this.pairs=[];
this.returnWrappers=_6c3||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_6c5,wrap,_6c7,_6c8){
var type=(_6c8)?"unshift":"push";
this.pairs[type]([name,_6c5,wrap,_6c7]);
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
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_6d0,wrap,_6d2){
dojo.json.jsonRegistry.register(name,_6d0,wrap,_6d2);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _6d5=typeof (o);
if(_6d5=="undefined"){
return "undefined";
}else{
if((_6d5=="number")||(_6d5=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_6d5=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _6d7;
if(typeof (o.__json__)=="function"){
_6d7=o.__json__();
if(o!==_6d7){
return me(_6d7);
}
}
if(typeof (o.json)=="function"){
_6d7=o.json();
if(o!==_6d7){
return me(_6d7);
}
}
if(_6d5!="function"&&typeof (o.length)=="number"){
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
_6d7=dojo.json.jsonRegistry.match(o);
return me(_6d7);
}
catch(e){
}
if(_6d5=="function"){
return null;
}
res=[];
for(var k in o){
var _6dc;
if(typeof (k)=="number"){
_6dc="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_6dc=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_6dc+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.Deferred");
dojo.Deferred=function(_6dd){
this.chain=[];
this.id=this._nextId();
this.fired=-1;
this.paused=0;
this.results=[null,null];
this.canceller=_6dd;
this.silentlyCancelled=false;
};
dojo.lang.extend(dojo.Deferred,{getFunctionFromArgs:function(){
var a=arguments;
if((a[0])&&(!a[1])){
if(dojo.lang.isFunction(a[0])){
return a[0];
}else{
if(dojo.lang.isString(a[0])){
return dj_global[a[0]];
}
}
}else{
if((a[0])&&(a[1])){
return dojo.lang.hitch(a[0],a[1]);
}
}
return null;
},makeCalled:function(){
var _6df=new dojo.Deferred();
_6df.callback();
return _6df;
},repr:function(){
var _6e0;
if(this.fired==-1){
_6e0="unfired";
}else{
if(this.fired==0){
_6e0="success";
}else{
_6e0="error";
}
}
return "Deferred("+this.id+", "+_6e0+")";
},toString:dojo.lang.forward("repr"),_nextId:(function(){
var n=1;
return function(){
return n++;
};
})(),cancel:function(){
if(this.fired==-1){
if(this.canceller){
this.canceller(this);
}else{
this.silentlyCancelled=true;
}
if(this.fired==-1){
this.errback(new Error(this.repr()));
}
}else{
if((this.fired==0)&&(this.results[0] instanceof dojo.Deferred)){
this.results[0].cancel();
}
}
},_pause:function(){
this.paused++;
},_unpause:function(){
this.paused--;
if((this.paused==0)&&(this.fired>=0)){
this._fire();
}
},_continue:function(res){
this._resback(res);
this._unpause();
},_resback:function(res){
this.fired=((res instanceof Error)?1:0);
this.results[this.fired]=res;
this._fire();
},_check:function(){
if(this.fired!=-1){
if(!this.silentlyCancelled){
dojo.raise("already called!");
}
this.silentlyCancelled=false;
return;
}
},callback:function(res){
this._check();
this._resback(res);
},errback:function(res){
this._check();
if(!(res instanceof Error)){
res=new Error(res);
}
this._resback(res);
},addBoth:function(cb,cbfn){
var _6e8=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_6e8=dojo.lang.curryArguments(null,_6e8,arguments,2);
}
return this.addCallbacks(_6e8,_6e8);
},addCallback:function(cb,cbfn){
var _6eb=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_6eb=dojo.lang.curryArguments(null,_6eb,arguments,2);
}
return this.addCallbacks(_6eb,null);
},addErrback:function(cb,cbfn){
var _6ee=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_6ee=dojo.lang.curryArguments(null,_6ee,arguments,2);
}
return this.addCallbacks(null,_6ee);
return this.addCallbacks(null,cbfn);
},addCallbacks:function(cb,eb){
this.chain.push([cb,eb]);
if(this.fired>=0){
this._fire();
}
return this;
},_fire:function(){
var _6f1=this.chain;
var _6f2=this.fired;
var res=this.results[_6f2];
var self=this;
var cb=null;
while(_6f1.length>0&&this.paused==0){
var pair=_6f1.shift();
var f=pair[_6f2];
if(f==null){
continue;
}
try{
res=f(res);
_6f2=((res instanceof Error)?1:0);
if(res instanceof dojo.Deferred){
cb=function(res){
self._continue(res);
};
this._pause();
}
}
catch(err){
_6f2=1;
res=err;
}
}
this.fired=_6f2;
this.results[_6f2]=res;
if((cb)&&(this.paused)){
res.addBoth(cb);
}
}});
dojo.provide("dojo.rpc.RpcService");
dojo.rpc.RpcService=function(url){
if(url){
this.connect(url);
}
};
dojo.lang.extend(dojo.rpc.RpcService,{strictArgChecks:true,serviceUrl:"",parseResults:function(obj){
return obj;
},errorCallback:function(_6fb){
return function(type,e){
_6fb.errback(new Error(e.message));
};
},resultCallback:function(_6fe){
var tf=dojo.lang.hitch(this,function(type,obj,e){
if(obj["error"]!=null){
var err=new Error(obj.error);
err.id=obj.id;
_6fe.errback(err);
}else{
var _704=this.parseResults(obj);
_6fe.callback(_704);
}
});
return tf;
},generateMethod:function(_705,_706,url){
return dojo.lang.hitch(this,function(){
var _708=new dojo.Deferred();
if((this.strictArgChecks)&&(_706!=null)&&(arguments.length!=_706.length)){
dojo.raise("Invalid number of parameters for remote method.");
}else{
this.bind(_705,arguments,_708,url);
}
return _708;
});
},processSmd:function(_709){
dojo.debug("RpcService: Processing returned SMD.");
if(_709.methods){
dojo.lang.forEach(_709.methods,function(m){
if(m&&m["name"]){
dojo.debug("RpcService: Creating Method: this.",m.name,"()");
this[m.name]=this.generateMethod(m.name,m.parameters,m["url"]||m["serviceUrl"]||m["serviceURL"]);
if(dojo.lang.isFunction(this[m.name])){
dojo.debug("RpcService: Successfully created",m.name,"()");
}else{
dojo.debug("RpcService: Failed to create",m.name,"()");
}
}
},this);
}
this.serviceUrl=_709.serviceUrl||_709.serviceURL;
dojo.debug("RpcService: Dojo RpcService is ready for use.");
},connect:function(_70b){
dojo.debug("RpcService: Attempting to load SMD document from:",_70b);
dojo.io.bind({url:_70b,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,_70d,e){
return this.processSmd(_70d);
}),sync:true});
}});
dojo.provide("dojo.rpc.JsonService");
dojo.rpc.JsonService=function(args){
if(args){
if(dojo.lang.isString(args)){
this.connect(args);
}else{
if(args["smdUrl"]){
this.connect(args.smdUrl);
}
if(args["smdStr"]){
this.processSmd(dj_eval("("+args.smdStr+")"));
}
if(args["smdObj"]){
this.processSmd(args.smdObj);
}
if(args["serviceUrl"]){
this.serviceUrl=args.serviceUrl;
}
if(typeof args["strictArgChecks"]!="undefined"){
this.strictArgChecks=args.strictArgChecks;
}
}
}
};
dojo.inherits(dojo.rpc.JsonService,dojo.rpc.RpcService);
dojo.extend(dojo.rpc.JsonService,{bustCache:false,contentType:"application/json-rpc",lastSubmissionId:0,callRemote:function(_710,_711){
var _712=new dojo.Deferred();
this.bind(_710,_711,_712);
return _712;
},bind:function(_713,_714,_715,url){
dojo.io.bind({url:url||this.serviceUrl,postContent:this.createRequest(_713,_714),method:"POST",contentType:this.contentType,mimetype:"text/json",load:this.resultCallback(_715),error:this.errorCallback(_715),preventCache:this.bustCache});
},createRequest:function(_717,_718){
var req={"params":_718,"method":_717,"id":++this.lastSubmissionId};
var data=dojo.json.serialize(req);
dojo.debug("JsonService: JSON-RPC Request: "+data);
return data;
},parseResults:function(obj){
if(!obj){
return;
}
if(obj["Result"]!=null){
return obj["Result"];
}else{
if(obj["result"]!=null){
return obj["result"];
}else{
if(obj["ResultSet"]){
return obj["ResultSet"];
}else{
return obj;
}
}
}
}});
dojo.provide("dojo.rpc.*");
dojo.provide("dojo.xml.Parse");
dojo.xml.Parse=function(){
var isIE=((dojo.render.html.capable)&&(dojo.render.html.ie));
function getTagName(node){
try{
return node.tagName.toLowerCase();
}
catch(e){
return "";
}
}
function getDojoTagName(node){
var _71f=getTagName(node);
if(!_71f){
return "";
}
if((dojo.widget)&&(dojo.widget.tags[_71f])){
return _71f;
}
var p=_71f.indexOf(":");
if(p>=0){
return _71f;
}
if(_71f.substr(0,5)=="dojo:"){
return _71f;
}
if(dojo.render.html.capable&&dojo.render.html.ie&&node.scopeName&&node.scopeName!="HTML"){
return node.scopeName.toLowerCase()+":"+_71f;
}
if(_71f.substr(0,4)=="dojo"){
return "dojo:"+_71f.substring(4);
}
var djt=node.getAttribute("dojoType")||node.getAttribute("dojotype");
if(djt){
if(djt.indexOf(":")<0){
djt="dojo:"+djt;
}
return djt.toLowerCase();
}
djt=node.getAttributeNS&&node.getAttributeNS(dojo.dom.dojoml,"type");
if(djt){
return "dojo:"+djt.toLowerCase();
}
try{
djt=node.getAttribute("dojo:type");
}
catch(e){
}
if(djt){
return "dojo:"+djt.toLowerCase();
}
if((dj_global["djConfig"])&&(!djConfig["ignoreClassNames"])){
var _722=node.className||node.getAttribute("class");
if((_722)&&(_722.indexOf)&&(_722.indexOf("dojo-")!=-1)){
var _723=_722.split(" ");
for(var x=0,c=_723.length;x<c;x++){
if(_723[x].slice(0,5)=="dojo-"){
return "dojo:"+_723[x].substr(5).toLowerCase();
}
}
}
}
return "";
}
this.parseElement=function(node,_727,_728,_729){
var _72a=getTagName(node);
if(isIE&&_72a.indexOf("/")==0){
return null;
}
try{
var attr=node.getAttribute("parseWidgets");
if(attr&&attr.toLowerCase()=="false"){
return {};
}
}
catch(e){
}
var _72c=true;
if(_728){
var _72d=getDojoTagName(node);
_72a=_72d||_72a;
_72c=Boolean(_72d);
}
var _72e={};
_72e[_72a]=[];
var pos=_72a.indexOf(":");
if(pos>0){
var ns=_72a.substring(0,pos);
_72e["ns"]=ns;
if((dojo.ns)&&(!dojo.ns.allow(ns))){
_72c=false;
}
}
if(_72c){
var _731=this.parseAttributes(node);
for(var attr in _731){
if((!_72e[_72a][attr])||(typeof _72e[_72a][attr]!="array")){
_72e[_72a][attr]=[];
}
_72e[_72a][attr].push(_731[attr]);
}
_72e[_72a].nodeRef=node;
_72e.tagName=_72a;
_72e.index=_729||0;
}
var _732=0;
for(var i=0;i<node.childNodes.length;i++){
var tcn=node.childNodes.item(i);
switch(tcn.nodeType){
case dojo.dom.ELEMENT_NODE:
var ctn=getDojoTagName(tcn)||getTagName(tcn);
if(!_72e[ctn]){
_72e[ctn]=[];
}
_72e[ctn].push(this.parseElement(tcn,true,_728,_732));
if((tcn.childNodes.length==1)&&(tcn.childNodes.item(0).nodeType==dojo.dom.TEXT_NODE)){
_72e[ctn][_72e[ctn].length-1].value=tcn.childNodes.item(0).nodeValue;
}
_732++;
break;
case dojo.dom.TEXT_NODE:
if(node.childNodes.length==1){
_72e[_72a].push({value:node.childNodes.item(0).nodeValue});
}
break;
default:
break;
}
}
return _72e;
};
this.parseAttributes=function(node){
var _737={};
var atts=node.attributes;
var _739,i=0;
while((_739=atts[i++])){
if(isIE){
if(!_739){
continue;
}
if((typeof _739=="object")&&(typeof _739.nodeValue=="undefined")||(_739.nodeValue==null)||(_739.nodeValue=="")){
continue;
}
}
var nn=_739.nodeName.split(":");
nn=(nn.length==2)?nn[1]:_739.nodeName;
_737[nn]={value:_739.nodeValue};
}
return _737;
};
};
dojo.provide("dojo.xml.*");
dojo.provide("dojo.undo.Manager");
dojo.undo.Manager=function(_73c){
this.clear();
this._parent=_73c;
};
dojo.extend(dojo.undo.Manager,{_parent:null,_undoStack:null,_redoStack:null,_currentManager:null,canUndo:false,canRedo:false,isUndoing:false,isRedoing:false,onUndo:function(_73d,item){
},onRedo:function(_73f,item){
},onUndoAny:function(_741,item){
},onRedoAny:function(_743,item){
},_updateStatus:function(){
this.canUndo=this._undoStack.length>0;
this.canRedo=this._redoStack.length>0;
},clear:function(){
this._undoStack=[];
this._redoStack=[];
this._currentManager=this;
this.isUndoing=false;
this.isRedoing=false;
this._updateStatus();
},undo:function(){
if(!this.canUndo){
return false;
}
this.endAllTransactions();
this.isUndoing=true;
var top=this._undoStack.pop();
if(top instanceof dojo.undo.Manager){
top.undoAll();
}else{
top.undo();
}
if(top.redo){
this._redoStack.push(top);
}
this.isUndoing=false;
this._updateStatus();
this.onUndo(this,top);
if(!(top instanceof dojo.undo.Manager)){
this.getTop().onUndoAny(this,top);
}
return true;
},redo:function(){
if(!this.canRedo){
return false;
}
this.isRedoing=true;
var top=this._redoStack.pop();
if(top instanceof dojo.undo.Manager){
top.redoAll();
}else{
top.redo();
}
this._undoStack.push(top);
this.isRedoing=false;
this._updateStatus();
this.onRedo(this,top);
if(!(top instanceof dojo.undo.Manager)){
this.getTop().onRedoAny(this,top);
}
return true;
},undoAll:function(){
while(this._undoStack.length>0){
this.undo();
}
},redoAll:function(){
while(this._redoStack.length>0){
this.redo();
}
},push:function(undo,redo,_749){
if(!undo){
return;
}
if(this._currentManager==this){
this._undoStack.push({undo:undo,redo:redo,description:_749});
}else{
this._currentManager.push.apply(this._currentManager,arguments);
}
this._redoStack=[];
this._updateStatus();
},concat:function(_74a){
if(!_74a){
return;
}
if(this._currentManager==this){
for(var x=0;x<_74a._undoStack.length;x++){
this._undoStack.push(_74a._undoStack[x]);
}
if(_74a._undoStack.length>0){
this._redoStack=[];
}
this._updateStatus();
}else{
this._currentManager.concat.apply(this._currentManager,arguments);
}
},beginTransaction:function(_74c){
if(this._currentManager==this){
var mgr=new dojo.undo.Manager(this);
mgr.description=_74c?_74c:"";
this._undoStack.push(mgr);
this._currentManager=mgr;
return mgr;
}else{
this._currentManager=this._currentManager.beginTransaction.apply(this._currentManager,arguments);
}
},endTransaction:function(_74e){
if(this._currentManager==this){
if(this._parent){
this._parent._currentManager=this._parent;
if(this._undoStack.length==0||_74e){
var idx=dojo.lang.find(this._parent._undoStack,this);
if(idx>=0){
this._parent._undoStack.splice(idx,1);
if(_74e){
for(var x=0;x<this._undoStack.length;x++){
this._parent._undoStack.splice(idx++,0,this._undoStack[x]);
}
this._updateStatus();
}
}
}
return this._parent;
}
}else{
this._currentManager=this._currentManager.endTransaction.apply(this._currentManager,arguments);
}
},endAllTransactions:function(){
while(this._currentManager!=this){
this.endTransaction();
}
},getTop:function(){
if(this._parent){
return this._parent.getTop();
}else{
return this;
}
}});
dojo.provide("dojo.undo.*");
dojo.provide("dojo.crypto");
dojo.crypto.cipherModes={ECB:0,CBC:1,PCBC:2,CFB:3,OFB:4,CTR:5};
dojo.crypto.outputTypes={Base64:0,Hex:1,String:2,Raw:3};
dojo.provide("dojo.crypto.MD5");
dojo.crypto.MD5=new function(){
var _751=8;
var mask=(1<<_751)-1;
function toWord(s){
var wa=[];
for(var i=0;i<s.length*_751;i+=_751){
wa[i>>5]|=(s.charCodeAt(i/_751)&mask)<<(i%32);
}
return wa;
}
function toString(wa){
var s=[];
for(var i=0;i<wa.length*32;i+=_751){
s.push(String.fromCharCode((wa[i>>5]>>>(i%32))&mask));
}
return s.join("");
}
function toHex(wa){
var h="0123456789abcdef";
var s=[];
for(var i=0;i<wa.length*4;i++){
s.push(h.charAt((wa[i>>2]>>((i%4)*8+4))&15)+h.charAt((wa[i>>2]>>((i%4)*8))&15));
}
return s.join("");
}
function toBase64(wa){
var p="=";
var tab="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var s=[];
for(var i=0;i<wa.length*4;i+=3){
var t=(((wa[i>>2]>>8*(i%4))&255)<<16)|(((wa[i+1>>2]>>8*((i+1)%4))&255)<<8)|((wa[i+2>>2]>>8*((i+2)%4))&255);
for(var j=0;j<4;j++){
if(i*8+j*6>wa.length*32){
s.push(p);
}else{
s.push(tab.charAt((t>>6*(3-j))&63));
}
}
}
return s.join("");
}
function add(x,y){
var l=(x&65535)+(y&65535);
var m=(x>>16)+(y>>16)+(l>>16);
return (m<<16)|(l&65535);
}
function R(n,c){
return (n<<c)|(n>>>(32-c));
}
function C(q,a,b,x,s,t){
return add(R(add(add(a,q),add(x,t)),s),b);
}
function FF(a,b,c,d,x,s,t){
return C((b&c)|((~b)&d),a,b,x,s,t);
}
function GG(a,b,c,d,x,s,t){
return C((b&d)|(c&(~d)),a,b,x,s,t);
}
function HH(a,b,c,d,x,s,t){
return C(b^c^d,a,b,x,s,t);
}
function II(a,b,c,d,x,s,t){
return C(c^(b|(~d)),a,b,x,s,t);
}
function core(x,len){
x[len>>5]|=128<<((len)%32);
x[(((len+64)>>>9)<<4)+14]=len;
var a=1732584193;
var b=-271733879;
var c=-1732584194;
var d=271733878;
for(var i=0;i<x.length;i+=16){
var olda=a;
var oldb=b;
var oldc=c;
var oldd=d;
a=FF(a,b,c,d,x[i+0],7,-680876936);
d=FF(d,a,b,c,x[i+1],12,-389564586);
c=FF(c,d,a,b,x[i+2],17,606105819);
b=FF(b,c,d,a,x[i+3],22,-1044525330);
a=FF(a,b,c,d,x[i+4],7,-176418897);
d=FF(d,a,b,c,x[i+5],12,1200080426);
c=FF(c,d,a,b,x[i+6],17,-1473231341);
b=FF(b,c,d,a,x[i+7],22,-45705983);
a=FF(a,b,c,d,x[i+8],7,1770035416);
d=FF(d,a,b,c,x[i+9],12,-1958414417);
c=FF(c,d,a,b,x[i+10],17,-42063);
b=FF(b,c,d,a,x[i+11],22,-1990404162);
a=FF(a,b,c,d,x[i+12],7,1804603682);
d=FF(d,a,b,c,x[i+13],12,-40341101);
c=FF(c,d,a,b,x[i+14],17,-1502002290);
b=FF(b,c,d,a,x[i+15],22,1236535329);
a=GG(a,b,c,d,x[i+1],5,-165796510);
d=GG(d,a,b,c,x[i+6],9,-1069501632);
c=GG(c,d,a,b,x[i+11],14,643717713);
b=GG(b,c,d,a,x[i+0],20,-373897302);
a=GG(a,b,c,d,x[i+5],5,-701558691);
d=GG(d,a,b,c,x[i+10],9,38016083);
c=GG(c,d,a,b,x[i+15],14,-660478335);
b=GG(b,c,d,a,x[i+4],20,-405537848);
a=GG(a,b,c,d,x[i+9],5,568446438);
d=GG(d,a,b,c,x[i+14],9,-1019803690);
c=GG(c,d,a,b,x[i+3],14,-187363961);
b=GG(b,c,d,a,x[i+8],20,1163531501);
a=GG(a,b,c,d,x[i+13],5,-1444681467);
d=GG(d,a,b,c,x[i+2],9,-51403784);
c=GG(c,d,a,b,x[i+7],14,1735328473);
b=GG(b,c,d,a,x[i+12],20,-1926607734);
a=HH(a,b,c,d,x[i+5],4,-378558);
d=HH(d,a,b,c,x[i+8],11,-2022574463);
c=HH(c,d,a,b,x[i+11],16,1839030562);
b=HH(b,c,d,a,x[i+14],23,-35309556);
a=HH(a,b,c,d,x[i+1],4,-1530992060);
d=HH(d,a,b,c,x[i+4],11,1272893353);
c=HH(c,d,a,b,x[i+7],16,-155497632);
b=HH(b,c,d,a,x[i+10],23,-1094730640);
a=HH(a,b,c,d,x[i+13],4,681279174);
d=HH(d,a,b,c,x[i+0],11,-358537222);
c=HH(c,d,a,b,x[i+3],16,-722521979);
b=HH(b,c,d,a,x[i+6],23,76029189);
a=HH(a,b,c,d,x[i+9],4,-640364487);
d=HH(d,a,b,c,x[i+12],11,-421815835);
c=HH(c,d,a,b,x[i+15],16,530742520);
b=HH(b,c,d,a,x[i+2],23,-995338651);
a=II(a,b,c,d,x[i+0],6,-198630844);
d=II(d,a,b,c,x[i+7],10,1126891415);
c=II(c,d,a,b,x[i+14],15,-1416354905);
b=II(b,c,d,a,x[i+5],21,-57434055);
a=II(a,b,c,d,x[i+12],6,1700485571);
d=II(d,a,b,c,x[i+3],10,-1894986606);
c=II(c,d,a,b,x[i+10],15,-1051523);
b=II(b,c,d,a,x[i+1],21,-2054922799);
a=II(a,b,c,d,x[i+8],6,1873313359);
d=II(d,a,b,c,x[i+15],10,-30611744);
c=II(c,d,a,b,x[i+6],15,-1560198380);
b=II(b,c,d,a,x[i+13],21,1309151649);
a=II(a,b,c,d,x[i+4],6,-145523070);
d=II(d,a,b,c,x[i+11],10,-1120210379);
c=II(c,d,a,b,x[i+2],15,718787259);
b=II(b,c,d,a,x[i+9],21,-343485551);
a=add(a,olda);
b=add(b,oldb);
c=add(c,oldc);
d=add(d,oldd);
}
return [a,b,c,d];
}
function hmac(data,key){
var wa=toWord(key);
if(wa.length>16){
wa=core(wa,key.length*_751);
}
var l=[],r=[];
for(var i=0;i<16;i++){
l[i]=wa[i]^909522486;
r[i]=wa[i]^1549556828;
}
var h=core(l.concat(toWord(data)),512+data.length*_751);
return core(r.concat(h),640);
}
this.compute=function(data,_79f){
var out=_79f||dojo.crypto.outputTypes.Base64;
switch(out){
case dojo.crypto.outputTypes.Hex:
return toHex(core(toWord(data),data.length*_751));
case dojo.crypto.outputTypes.String:
return toString(core(toWord(data),data.length*_751));
default:
return toBase64(core(toWord(data),data.length*_751));
}
};
this.getHMAC=function(data,key,_7a3){
var out=_7a3||dojo.crypto.outputTypes.Base64;
switch(out){
case dojo.crypto.outputTypes.Hex:
return toHex(hmac(data,key));
case dojo.crypto.outputTypes.String:
return toString(hmac(data,key));
default:
return toBase64(hmac(data,key));
}
};
}();
dojo.provide("dojo.crypto.*");
dojo.provide("dojo.collections.Collections");
dojo.collections.DictionaryEntry=function(k,v){
this.key=k;
this.value=v;
this.valueOf=function(){
return this.value;
};
this.toString=function(){
return String(this.value);
};
};
dojo.collections.Iterator=function(arr){
var a=arr;
var _7a9=0;
this.element=a[_7a9]||null;
this.atEnd=function(){
return (_7a9>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_7a9++];
return this.element;
};
this.map=function(fn,_7ab){
var s=_7ab||dj_global;
if(Array.map){
return Array.map(a,fn,s);
}else{
var arr=[];
for(var i=0;i<a.length;i++){
arr.push(fn.call(s,a[i]));
}
return arr;
}
};
this.reset=function(){
_7a9=0;
this.element=a[_7a9];
};
};
dojo.collections.DictionaryIterator=function(obj){
var a=[];
var _7b1={};
for(var p in obj){
if(!_7b1[p]){
a.push(obj[p]);
}
}
var _7b3=0;
this.element=a[_7b3]||null;
this.atEnd=function(){
return (_7b3>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_7b3++];
return this.element;
};
this.map=function(fn,_7b5){
var s=_7b5||dj_global;
if(Array.map){
return Array.map(a,fn,s);
}else{
var arr=[];
for(var i=0;i<a.length;i++){
arr.push(fn.call(s,a[i]));
}
return arr;
}
};
this.reset=function(){
_7b3=0;
this.element=a[_7b3];
};
};
dojo.provide("dojo.collections.ArrayList");
dojo.collections.ArrayList=function(arr){
var _7ba=[];
if(arr){
_7ba=_7ba.concat(arr);
}
this.count=_7ba.length;
this.add=function(obj){
_7ba.push(obj);
this.count=_7ba.length;
};
this.addRange=function(a){
if(a.getIterator){
var e=a.getIterator();
while(!e.atEnd()){
this.add(e.get());
}
this.count=_7ba.length;
}else{
for(var i=0;i<a.length;i++){
_7ba.push(a[i]);
}
this.count=_7ba.length;
}
};
this.clear=function(){
_7ba.splice(0,_7ba.length);
this.count=0;
};
this.clone=function(){
return new dojo.collections.ArrayList(_7ba);
};
this.contains=function(obj){
for(var i=0;i<_7ba.length;i++){
if(_7ba[i]==obj){
return true;
}
}
return false;
};
this.forEach=function(fn,_7c2){
var s=_7c2||dj_global;
if(Array.forEach){
Array.forEach(_7ba,fn,s);
}else{
for(var i=0;i<_7ba.length;i++){
fn.call(s,_7ba[i],i,_7ba);
}
}
};
this.getIterator=function(){
return new dojo.collections.Iterator(_7ba);
};
this.indexOf=function(obj){
for(var i=0;i<_7ba.length;i++){
if(_7ba[i]==obj){
return i;
}
}
return -1;
};
this.insert=function(i,obj){
_7ba.splice(i,0,obj);
this.count=_7ba.length;
};
this.item=function(i){
return _7ba[i];
};
this.remove=function(obj){
var i=this.indexOf(obj);
if(i>=0){
_7ba.splice(i,1);
}
this.count=_7ba.length;
};
this.removeAt=function(i){
_7ba.splice(i,1);
this.count=_7ba.length;
};
this.reverse=function(){
_7ba.reverse();
};
this.sort=function(fn){
if(fn){
_7ba.sort(fn);
}else{
_7ba.sort();
}
};
this.setByIndex=function(i,obj){
_7ba[i]=obj;
this.count=_7ba.length;
};
this.toArray=function(){
return [].concat(_7ba);
};
this.toString=function(_7d0){
return _7ba.join((_7d0||","));
};
};
dojo.provide("dojo.collections.Queue");
dojo.collections.Queue=function(arr){
var q=[];
if(arr){
q=q.concat(arr);
}
this.count=q.length;
this.clear=function(){
q=[];
this.count=q.length;
};
this.clone=function(){
return new dojo.collections.Queue(q);
};
this.contains=function(o){
for(var i=0;i<q.length;i++){
if(q[i]==o){
return true;
}
}
return false;
};
this.copyTo=function(arr,i){
arr.splice(i,0,q);
};
this.dequeue=function(){
var r=q.shift();
this.count=q.length;
return r;
};
this.enqueue=function(o){
this.count=q.push(o);
};
this.forEach=function(fn,_7da){
var s=_7da||dj_global;
if(Array.forEach){
Array.forEach(q,fn,s);
}else{
for(var i=0;i<q.length;i++){
fn.call(s,q[i],i,q);
}
}
};
this.getIterator=function(){
return new dojo.collections.Iterator(q);
};
this.peek=function(){
return q[0];
};
this.toArray=function(){
return [].concat(q);
};
};
dojo.provide("dojo.collections.Stack");
dojo.collections.Stack=function(arr){
var q=[];
if(arr){
q=q.concat(arr);
}
this.count=q.length;
this.clear=function(){
q=[];
this.count=q.length;
};
this.clone=function(){
return new dojo.collections.Stack(q);
};
this.contains=function(o){
for(var i=0;i<q.length;i++){
if(q[i]==o){
return true;
}
}
return false;
};
this.copyTo=function(arr,i){
arr.splice(i,0,q);
};
this.forEach=function(fn,_7e4){
var s=_7e4||dj_global;
if(Array.forEach){
Array.forEach(q,fn,s);
}else{
for(var i=0;i<q.length;i++){
fn.call(s,q[i],i,q);
}
}
};
this.getIterator=function(){
return new dojo.collections.Iterator(q);
};
this.peek=function(){
return q[(q.length-1)];
};
this.pop=function(){
var r=q.pop();
this.count=q.length;
return r;
};
this.push=function(o){
this.count=q.push(o);
};
this.toArray=function(){
return [].concat(q);
};
};
dojo.provide("dojo.dnd.DragAndDrop");
dojo.declare("dojo.dnd.DragSource",null,{type:"",onDragEnd:function(evt){
},onDragStart:function(evt){
},onSelected:function(evt){
},unregister:function(){
dojo.dnd.dragManager.unregisterDragSource(this);
},reregister:function(){
dojo.dnd.dragManager.registerDragSource(this);
}});
dojo.declare("dojo.dnd.DragObject",null,{type:"",register:function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragObject"]){
dm.registerDragObject(this);
}
},onDragStart:function(evt){
},onDragMove:function(evt){
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragEnd:function(evt){
},onDragLeave:dojo.lang.forward("onDragOut"),onDragEnter:dojo.lang.forward("onDragOver"),ondragout:dojo.lang.forward("onDragOut"),ondragover:dojo.lang.forward("onDragOver")});
dojo.declare("dojo.dnd.DropTarget",null,function(){
this.acceptedTypes=[];
},{acceptsType:function(type){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
if(!dojo.lang.inArray(this.acceptedTypes,type)){
return false;
}
}
return true;
},accepts:function(_7f3){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
for(var i=0;i<_7f3.length;i++){
if(!dojo.lang.inArray(this.acceptedTypes,_7f3[i].type)){
return false;
}
}
}
return true;
},unregister:function(){
dojo.dnd.dragManager.unregisterDropTarget(this);
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragMove:function(evt){
},onDropStart:function(evt){
},onDrop:function(evt){
},onDropEnd:function(){
}});
dojo.dnd.DragEvent=function(){
this.dragSource=null;
this.dragObject=null;
this.target=null;
this.eventStatus="success";
};
dojo.declare("dojo.dnd.DragManager",null,{selectedSources:[],dragObjects:[],dragSources:[],registerDragSource:function(_7fa){
},dropTargets:[],registerDropTarget:function(_7fb){
},lastDragTarget:null,currentDragTarget:null,onKeyDown:function(){
},onMouseOut:function(){
},onMouseMove:function(){
},onMouseUp:function(){
}});
dojo.provide("dojo.dnd.HtmlDragManager");
dojo.declare("dojo.dnd.HtmlDragManager",dojo.dnd.DragManager,{disabled:false,nestedTargets:false,mouseDownTimer:null,dsCounter:0,dsPrefix:"dojoDragSource",dropTargetDimensions:[],currentDropTarget:null,previousDropTarget:null,_dragTriggered:false,selectedSources:[],dragObjects:[],dragSources:[],currentX:null,currentY:null,lastX:null,lastY:null,mouseDownX:null,mouseDownY:null,threshold:7,dropAcceptable:false,cancelEvent:function(e){
e.stopPropagation();
e.preventDefault();
},registerDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _7ff=dp+"Idx_"+(this.dsCounter++);
ds.dragSourceId=_7ff;
this.dragSources[_7ff]=ds;
ds.domNode.setAttribute(dp,_7ff);
if(dojo.render.html.ie){
dojo.event.browser.addListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},unregisterDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _802=ds.dragSourceId;
delete ds.dragSourceId;
delete this.dragSources[_802];
ds.domNode.setAttribute(dp,null);
if(dojo.render.html.ie){
dojo.event.browser.removeListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},registerDropTarget:function(dt){
this.dropTargets.push(dt);
},unregisterDropTarget:function(dt){
var _805=dojo.lang.find(this.dropTargets,dt,true);
if(_805>=0){
this.dropTargets.splice(_805,1);
}
},getDragSource:function(e){
var tn=e.target;
if(tn===dojo.body()){
return;
}
var ta=dojo.html.getAttribute(tn,this.dsPrefix);
while((!ta)&&(tn)){
tn=tn.parentNode;
if((!tn)||(tn===dojo.body())){
return;
}
ta=dojo.html.getAttribute(tn,this.dsPrefix);
}
return this.dragSources[ta];
},onKeyDown:function(e){
},onMouseDown:function(e){
if(this.disabled){
return;
}
if(dojo.render.html.ie){
if(e.button!=1){
return;
}
}else{
if(e.which!=1){
return;
}
}
var _80b=e.target.nodeType==dojo.html.TEXT_NODE?e.target.parentNode:e.target;
if(dojo.html.isTag(_80b,"button","textarea","input","select","option")){
return;
}
var ds=this.getDragSource(e);
if(!ds){
return;
}
if(!dojo.lang.inArray(this.selectedSources,ds)){
this.selectedSources.push(ds);
ds.onSelected();
}
this.mouseDownX=e.pageX;
this.mouseDownY=e.pageY;
e.preventDefault();
dojo.event.connect(document,"onmousemove",this,"onMouseMove");
},onMouseUp:function(e,_80e){
if(this.selectedSources.length==0){
return;
}
this.mouseDownX=null;
this.mouseDownY=null;
this._dragTriggered=false;
e.dragSource=this.dragSource;
if((!e.shiftKey)&&(!e.ctrlKey)){
if(this.currentDropTarget){
this.currentDropTarget.onDropStart();
}
dojo.lang.forEach(this.dragObjects,function(_80f){
var ret=null;
if(!_80f){
return;
}
if(this.currentDropTarget){
e.dragObject=_80f;
var ce=this.currentDropTarget.domNode.childNodes;
if(ce.length>0){
e.dropTarget=ce[0];
while(e.dropTarget==_80f.domNode){
e.dropTarget=e.dropTarget.nextSibling;
}
}else{
e.dropTarget=this.currentDropTarget.domNode;
}
if(this.dropAcceptable){
ret=this.currentDropTarget.onDrop(e);
}else{
this.currentDropTarget.onDragOut(e);
}
}
e.dragStatus=this.dropAcceptable&&ret?"dropSuccess":"dropFailure";
dojo.lang.delayThese([function(){
try{
_80f.dragSource.onDragEnd(e);
}
catch(err){
var _812={};
for(var i in e){
if(i=="type"){
_812.type="mouseup";
continue;
}
_812[i]=e[i];
}
_80f.dragSource.onDragEnd(_812);
}
},function(){
_80f.onDragEnd(e);
}]);
},this);
this.selectedSources=[];
this.dragObjects=[];
this.dragSource=null;
if(this.currentDropTarget){
this.currentDropTarget.onDropEnd();
}
}else{
}
dojo.event.disconnect(document,"onmousemove",this,"onMouseMove");
this.currentDropTarget=null;
},onScroll:function(){
for(var i=0;i<this.dragObjects.length;i++){
if(this.dragObjects[i].updateDragOffset){
this.dragObjects[i].updateDragOffset();
}
}
if(this.dragObjects.length){
this.cacheTargetLocations();
}
},_dragStartDistance:function(x,y){
if((!this.mouseDownX)||(!this.mouseDownX)){
return;
}
var dx=Math.abs(x-this.mouseDownX);
var dx2=dx*dx;
var dy=Math.abs(y-this.mouseDownY);
var dy2=dy*dy;
return parseInt(Math.sqrt(dx2+dy2),10);
},cacheTargetLocations:function(){
dojo.profile.start("cacheTargetLocations");
this.dropTargetDimensions=[];
dojo.lang.forEach(this.dropTargets,function(_81b){
var tn=_81b.domNode;
if(!tn||!_81b.accepts([this.dragSource])){
return;
}
var abs=dojo.html.getAbsolutePosition(tn,true);
var bb=dojo.html.getBorderBox(tn);
this.dropTargetDimensions.push([[abs.x,abs.y],[abs.x+bb.width,abs.y+bb.height],_81b]);
},this);
dojo.profile.end("cacheTargetLocations");
},onMouseMove:function(e){
if((dojo.render.html.ie)&&(e.button!=1)){
this.currentDropTarget=null;
this.onMouseUp(e,true);
return;
}
if((this.selectedSources.length)&&(!this.dragObjects.length)){
var dx;
var dy;
if(!this._dragTriggered){
this._dragTriggered=(this._dragStartDistance(e.pageX,e.pageY)>this.threshold);
if(!this._dragTriggered){
return;
}
dx=e.pageX-this.mouseDownX;
dy=e.pageY-this.mouseDownY;
}
this.dragSource=this.selectedSources[0];
dojo.lang.forEach(this.selectedSources,function(_822){
if(!_822){
return;
}
var tdo=_822.onDragStart(e);
if(tdo){
tdo.onDragStart(e);
tdo.dragOffset.y+=dy;
tdo.dragOffset.x+=dx;
tdo.dragSource=_822;
this.dragObjects.push(tdo);
}
},this);
this.previousDropTarget=null;
this.cacheTargetLocations();
}
dojo.lang.forEach(this.dragObjects,function(_824){
if(_824){
_824.onDragMove(e);
}
});
if(this.currentDropTarget){
var c=dojo.html.toCoordinateObject(this.currentDropTarget.domNode,true);
var dtp=[[c.x,c.y],[c.x+c.width,c.y+c.height]];
}
if((!this.nestedTargets)&&(dtp)&&(this.isInsideBox(e,dtp))){
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}else{
var _827=this.findBestTarget(e);
if(_827.target===null){
if(this.currentDropTarget){
this.currentDropTarget.onDragOut(e);
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget=null;
}
this.dropAcceptable=false;
return;
}
if(this.currentDropTarget!==_827.target){
if(this.currentDropTarget){
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget.onDragOut(e);
}
this.currentDropTarget=_827.target;
e.dragObjects=this.dragObjects;
this.dropAcceptable=this.currentDropTarget.onDragOver(e);
}else{
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}
}
},findBestTarget:function(e){
var _829=this;
var _82a=new Object();
_82a.target=null;
_82a.points=null;
dojo.lang.every(this.dropTargetDimensions,function(_82b){
if(!_829.isInsideBox(e,_82b)){
return true;
}
_82a.target=_82b[2];
_82a.points=_82b;
return Boolean(_829.nestedTargets);
});
return _82a;
},isInsideBox:function(e,_82d){
if((e.pageX>_82d[0][0])&&(e.pageX<_82d[1][0])&&(e.pageY>_82d[0][1])&&(e.pageY<_82d[1][1])){
return true;
}
return false;
},onMouseOver:function(e){
},onMouseOut:function(e){
}});
dojo.dnd.dragManager=new dojo.dnd.HtmlDragManager();
(function(){
var d=document;
var dm=dojo.dnd.dragManager;
dojo.event.connect(d,"onkeydown",dm,"onKeyDown");
dojo.event.connect(d,"onmouseover",dm,"onMouseOver");
dojo.event.connect(d,"onmouseout",dm,"onMouseOut");
dojo.event.connect(d,"onmousedown",dm,"onMouseDown");
dojo.event.connect(d,"onmouseup",dm,"onMouseUp");
dojo.event.connect(window,"onscroll",dm,"onScroll");
})();
dojo.provide("dojo.html.selection");
dojo.html.selectionType={NONE:0,TEXT:1,CONTROL:2};
dojo.html.clearSelection=function(){
var _832=dojo.global();
var _833=dojo.doc();
try{
if(_832["getSelection"]){
if(dojo.render.html.safari){
_832.getSelection().collapse();
}else{
_832.getSelection().removeAllRanges();
}
}else{
if(_833.selection){
if(_833.selection.empty){
_833.selection.empty();
}else{
if(_833.selection.clear){
_833.selection.clear();
}
}
}
}
return true;
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.html.disableSelection=function(_834){
_834=dojo.byId(_834)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_834.style.MozUserSelect="none";
}else{
if(h.safari){
_834.style.KhtmlUserSelect="none";
}else{
if(h.ie){
_834.unselectable="on";
}else{
return false;
}
}
}
return true;
};
dojo.html.enableSelection=function(_836){
_836=dojo.byId(_836)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_836.style.MozUserSelect="";
}else{
if(h.safari){
_836.style.KhtmlUserSelect="";
}else{
if(h.ie){
_836.unselectable="off";
}else{
return false;
}
}
}
return true;
};
dojo.html.selectInputText=function(_838){
var _839=dojo.global();
var _83a=dojo.doc();
_838=dojo.byId(_838);
if(_83a["selection"]&&dojo.body()["createTextRange"]){
var _83b=_838.createTextRange();
_83b.moveStart("character",0);
_83b.moveEnd("character",_838.value.length);
_83b.select();
}else{
if(_839["getSelection"]){
var _83c=_839.getSelection();
_838.setSelectionRange(0,_838.value.length);
}
}
_838.focus();
};
dojo.lang.mixin(dojo.html.selection,{getType:function(){
if(dojo.doc()["selection"]){
return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
}else{
var _83d=dojo.html.selectionType.TEXT;
var oSel;
try{
oSel=dojo.global().getSelection();
}
catch(e){
}
if(oSel&&oSel.rangeCount==1){
var _83f=oSel.getRangeAt(0);
if(_83f.startContainer==_83f.endContainer&&(_83f.endOffset-_83f.startOffset)==1&&_83f.startContainer.nodeType!=dojo.dom.TEXT_NODE){
_83d=dojo.html.selectionType.CONTROL;
}
}
return _83d;
}
},isCollapsed:function(){
var _840=dojo.global();
var _841=dojo.doc();
if(_841["selection"]){
return _841.selection.createRange().text=="";
}else{
if(_840["getSelection"]){
var _842=_840.getSelection();
if(dojo.lang.isString(_842)){
return _842=="";
}else{
return _842.isCollapsed||_842.toString()=="";
}
}
}
},getSelectedElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
if(dojo.doc()["selection"]){
var _843=dojo.doc().selection.createRange();
if(_843&&_843.item){
return dojo.doc().selection.createRange().item(0);
}
}else{
var _844=dojo.global().getSelection();
return _844.anchorNode.childNodes[_844.anchorOffset];
}
}
},getParentElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
var p=dojo.html.selection.getSelectedElement();
if(p){
return p.parentNode;
}
}else{
if(dojo.doc()["selection"]){
return dojo.doc().selection.createRange().parentElement();
}else{
var _846=dojo.global().getSelection();
if(_846){
var node=_846.anchorNode;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.parentNode;
}
return node;
}
}
}
},getSelectedText:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().text;
}else{
var _848=dojo.global().getSelection();
if(_848){
return _848.toString();
}
}
},getSelectedHtml:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().htmlText;
}else{
var _849=dojo.global().getSelection();
if(_849&&_849.rangeCount){
var frag=_849.getRangeAt(0).cloneContents();
var div=document.createElement("div");
div.appendChild(frag);
return div.innerHTML;
}
return null;
}
},hasAncestorElement:function(_84c){
return (dojo.html.selection.getAncestorElement.apply(this,arguments)!=null);
},getAncestorElement:function(_84d){
var node=dojo.html.selection.getSelectedElement()||dojo.html.selection.getParentElement();
while(node){
if(dojo.html.selection.isTag(node,arguments).length>0){
return node;
}
node=node.parentNode;
}
return null;
},isTag:function(node,tags){
if(node&&node.tagName){
for(var i=0;i<tags.length;i++){
if(node.tagName.toLowerCase()==String(tags[i]).toLowerCase()){
return String(tags[i]).toLowerCase();
}
}
}
return "";
},selectElement:function(_852){
var _853=dojo.global();
var _854=dojo.doc();
_852=dojo.byId(_852);
if(_854.selection&&dojo.body().createTextRange){
try{
var _855=dojo.body().createControlRange();
_855.addElement(_852);
_855.select();
}
catch(e){
dojo.html.selection.selectElementChildren(_852);
}
}else{
if(_853["getSelection"]){
var _856=_853.getSelection();
if(_856["removeAllRanges"]){
var _855=_854.createRange();
_855.selectNode(_852);
_856.removeAllRanges();
_856.addRange(_855);
}
}
}
},selectElementChildren:function(_857){
var _858=dojo.global();
var _859=dojo.doc();
_857=dojo.byId(_857);
if(_859.selection&&dojo.body().createTextRange){
var _85a=dojo.body().createTextRange();
_85a.moveToElementText(_857);
_85a.select();
}else{
if(_858["getSelection"]){
var _85b=_858.getSelection();
if(_85b["setBaseAndExtent"]){
_85b.setBaseAndExtent(_857,0,_857,_857.innerText.length-1);
}else{
if(_85b["selectAllChildren"]){
_85b.selectAllChildren(_857);
}
}
}
}
},getBookmark:function(){
var _85c;
var _85d=dojo.doc();
if(_85d["selection"]){
var _85e=_85d.selection.createRange();
_85c=_85e.getBookmark();
}else{
var _85f;
try{
_85f=dojo.global().getSelection();
}
catch(e){
}
if(_85f){
var _85e=_85f.getRangeAt(0);
_85c=_85e.cloneRange();
}else{
dojo.debug("No idea how to store the current selection for this browser!");
}
}
return _85c;
},moveToBookmark:function(_860){
var _861=dojo.doc();
if(_861["selection"]){
var _862=_861.selection.createRange();
_862.moveToBookmark(_860);
_862.select();
}else{
var _863;
try{
_863=dojo.global().getSelection();
}
catch(e){
}
if(_863&&_863["removeAllRanges"]){
_863.removeAllRanges();
_863.addRange(_860);
}else{
dojo.debug("No idea how to restore selection for this browser!");
}
}
},collapse:function(_864){
if(dojo.global()["getSelection"]){
var _865=dojo.global().getSelection();
if(_865.removeAllRanges){
if(_864){
_865.collapseToStart();
}else{
_865.collapseToEnd();
}
}else{
dojo.global().getSelection().collapse(_864);
}
}else{
if(dojo.doc().selection){
var _866=dojo.doc().selection.createRange();
_866.collapse(_864);
_866.select();
}
}
},remove:function(){
if(dojo.doc().selection){
var _867=dojo.doc().selection;
if(_867.type.toUpperCase()!="NONE"){
_867.clear();
}
return _867;
}else{
var _867=dojo.global().getSelection();
for(var i=0;i<_867.rangeCount;i++){
_867.getRangeAt(i).deleteContents();
}
return _867;
}
}});
dojo.provide("dojo.html.iframe");
dojo.html.iframeContentWindow=function(_869){
var win=dojo.html.getDocumentWindow(dojo.html.iframeContentDocument(_869))||dojo.html.iframeContentDocument(_869).__parent__||(_869.name&&document.frames[_869.name])||null;
return win;
};
dojo.html.iframeContentDocument=function(_86b){
var doc=_86b.contentDocument||((_86b.contentWindow)&&(_86b.contentWindow.document))||((_86b.name)&&(document.frames[_86b.name])&&(document.frames[_86b.name].document))||null;
return doc;
};
dojo.html.BackgroundIframe=function(node){
if(dojo.render.html.ie55||dojo.render.html.ie60){
var html="<iframe src='javascript:false'"+" style='position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"+"z-index: -1; filter:Alpha(Opacity=\"0\");' "+">";
this.iframe=dojo.doc().createElement(html);
this.iframe.tabIndex=-1;
if(node){
node.appendChild(this.iframe);
this.domNode=node;
}else{
dojo.body().appendChild(this.iframe);
this.iframe.style.display="none";
}
}
};
dojo.lang.extend(dojo.html.BackgroundIframe,{iframe:null,onResized:function(){
if(this.iframe&&this.domNode&&this.domNode.parentNode){
var _86f=dojo.html.getMarginBox(this.domNode);
if(_86f.width==0||_86f.height==0){
dojo.lang.setTimeout(this,this.onResized,100);
return;
}
this.iframe.style.width=_86f.width+"px";
this.iframe.style.height=_86f.height+"px";
}
},size:function(node){
if(!this.iframe){
return;
}
var _871=dojo.html.toCoordinateObject(node,true,dojo.html.boxSizing.BORDER_BOX);
with(this.iframe.style){
width=_871.width+"px";
height=_871.height+"px";
left=_871.left+"px";
top=_871.top+"px";
}
},setZIndex:function(node){
if(!this.iframe){
return;
}
if(dojo.dom.isNode(node)){
this.iframe.style.zIndex=dojo.html.getStyle(node,"z-index")-1;
}else{
if(!isNaN(node)){
this.iframe.style.zIndex=node;
}
}
},show:function(){
if(this.iframe){
this.iframe.style.display="block";
}
},hide:function(){
if(this.iframe){
this.iframe.style.display="none";
}
},remove:function(){
if(this.iframe){
dojo.html.removeNode(this.iframe,true);
delete this.iframe;
this.iframe=null;
}
}});
dojo.provide("dojo.dnd.HtmlDragAndDrop");
dojo.declare("dojo.dnd.HtmlDragSource",dojo.dnd.DragSource,function(node,type){
node=dojo.byId(node);
this.dragObjects=[];
this.constrainToContainer=false;
if(node){
this.domNode=node;
this.dragObject=node;
this.type=(type)||(this.domNode.nodeName.toLowerCase());
dojo.dnd.DragSource.prototype.reregister.call(this);
}
},{dragClass:"",onDragStart:function(){
var _875=new dojo.dnd.HtmlDragObject(this.dragObject,this.type);
if(this.dragClass){
_875.dragClass=this.dragClass;
}
if(this.constrainToContainer){
_875.constrainTo(this.constrainingContainer||this.domNode.parentNode);
}
return _875;
},setDragHandle:function(node){
node=dojo.byId(node);
dojo.dnd.dragManager.unregisterDragSource(this);
this.domNode=node;
dojo.dnd.dragManager.registerDragSource(this);
},setDragTarget:function(node){
this.dragObject=node;
},constrainTo:function(_878){
this.constrainToContainer=true;
if(_878){
this.constrainingContainer=_878;
}
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragSource(this.dragObjects[i]));
}
},addDragObjects:function(el){
for(var i=0;i<arguments.length;i++){
this.dragObjects.push(dojo.byId(arguments[i]));
}
}});
dojo.declare("dojo.dnd.HtmlDragObject",dojo.dnd.DragObject,function(node,type){
this.domNode=dojo.byId(node);
this.type=type;
this.constrainToContainer=false;
this.dragSource=null;
dojo.dnd.DragObject.prototype.register.call(this);
},{dragClass:"",opacity:0.5,createIframe:true,disableX:false,disableY:false,createDragNode:function(){
var node=this.domNode.cloneNode(true);
if(this.dragClass){
dojo.html.addClass(node,this.dragClass);
}
if(this.opacity<1){
dojo.html.setOpacity(node,this.opacity);
}
var ltn=node.tagName.toLowerCase();
var isTr=(ltn=="tr");
if((isTr)||(ltn=="tbody")){
var doc=this.domNode.ownerDocument;
var _882=doc.createElement("table");
if(isTr){
var _883=doc.createElement("tbody");
_882.appendChild(_883);
_883.appendChild(node);
}else{
_882.appendChild(node);
}
var _884=((isTr)?this.domNode:this.domNode.firstChild);
var _885=((isTr)?node:node.firstChild);
var _886=tdp.childNodes;
var _887=_885.childNodes;
for(var i=0;i<_886.length;i++){
if((_887[i])&&(_887[i].style)){
_887[i].style.width=dojo.html.getContentBox(_886[i]).width+"px";
}
}
node=_882;
}
if((dojo.render.html.ie55||dojo.render.html.ie60)&&this.createIframe){
with(node.style){
top="0px";
left="0px";
}
var _889=document.createElement("div");
_889.appendChild(node);
this.bgIframe=new dojo.html.BackgroundIframe(_889);
_889.appendChild(this.bgIframe.iframe);
node=_889;
}
node.style.zIndex=999;
return node;
},onDragStart:function(e){
dojo.html.clearSelection();
this.scrollOffset=dojo.html.getScroll().offset;
this.dragStartPosition=dojo.html.getAbsolutePosition(this.domNode,true);
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.dragClone=this.createDragNode();
this.containingBlockPosition=this.domNode.offsetParent?dojo.html.getAbsolutePosition(this.domNode.offsetParent,true):{x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
with(this.dragClone.style){
position="absolute";
top=this.dragOffset.y+e.pageY+"px";
left=this.dragOffset.x+e.pageX+"px";
}
dojo.body().appendChild(this.dragClone);
dojo.event.topic.publish("dragStart",{source:this});
},getConstraints:function(){
if(this.constrainingContainer.nodeName.toLowerCase()=="body"){
var _88b=dojo.html.getViewport();
var _88c=_88b.width;
var _88d=_88b.height;
var _88e=dojo.html.getScroll().offset;
var x=_88e.x;
var y=_88e.y;
}else{
var _891=dojo.html.getContentBox(this.constrainingContainer);
_88c=_891.width;
_88d=_891.height;
x=this.containingBlockPosition.x+dojo.html.getPixelValue(this.constrainingContainer,"padding-left",true)+dojo.html.getBorderExtent(this.constrainingContainer,"left");
y=this.containingBlockPosition.y+dojo.html.getPixelValue(this.constrainingContainer,"padding-top",true)+dojo.html.getBorderExtent(this.constrainingContainer,"top");
}
var mb=dojo.html.getMarginBox(this.domNode);
return {minX:x,minY:y,maxX:x+_88c-mb.width,maxY:y+_88d-mb.height};
},updateDragOffset:function(){
var _893=dojo.html.getScroll().offset;
if(_893.y!=this.scrollOffset.y){
var diff=_893.y-this.scrollOffset.y;
this.dragOffset.y+=diff;
this.scrollOffset.y=_893.y;
}
if(_893.x!=this.scrollOffset.x){
var diff=_893.x-this.scrollOffset.x;
this.dragOffset.x+=diff;
this.scrollOffset.x=_893.x;
}
},onDragMove:function(e){
this.updateDragOffset();
var x=this.dragOffset.x+e.pageX;
var y=this.dragOffset.y+e.pageY;
if(this.constrainToContainer){
if(x<this.constraints.minX){
x=this.constraints.minX;
}
if(y<this.constraints.minY){
y=this.constraints.minY;
}
if(x>this.constraints.maxX){
x=this.constraints.maxX;
}
if(y>this.constraints.maxY){
y=this.constraints.maxY;
}
}
this.setAbsolutePosition(x,y);
dojo.event.topic.publish("dragMove",{source:this});
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.dragClone.style.top=y+"px";
}
if(!this.disableX){
this.dragClone.style.left=x+"px";
}
},onDragEnd:function(e){
switch(e.dragStatus){
case "dropSuccess":
dojo.html.removeNode(this.dragClone);
this.dragClone=null;
break;
case "dropFailure":
var _89b=dojo.html.getAbsolutePosition(this.dragClone,true);
var _89c={left:this.dragStartPosition.x+1,top:this.dragStartPosition.y+1};
var anim=dojo.lfx.slideTo(this.dragClone,_89c,300);
var _89e=this;
dojo.event.connect(anim,"onEnd",function(e){
dojo.html.removeNode(_89e.dragClone);
_89e.dragClone=null;
});
anim.play();
break;
}
dojo.event.topic.publish("dragEnd",{source:this});
},constrainTo:function(_8a0){
this.constrainToContainer=true;
if(_8a0){
this.constrainingContainer=_8a0;
}else{
this.constrainingContainer=this.domNode.parentNode;
}
}});
dojo.declare("dojo.dnd.HtmlDropTarget",dojo.dnd.DropTarget,function(node,_8a2){
if(arguments.length==0){
return;
}
this.domNode=dojo.byId(node);
dojo.dnd.DropTarget.call(this);
if(_8a2&&dojo.lang.isString(_8a2)){
_8a2=[_8a2];
}
this.acceptedTypes=_8a2||[];
dojo.dnd.dragManager.registerDropTarget(this);
},{vertical:false,onDragOver:function(e){
if(!this.accepts(e.dragObjects)){
return false;
}
this.childBoxes=[];
for(var i=0,_8a5;i<this.domNode.childNodes.length;i++){
_8a5=this.domNode.childNodes[i];
if(_8a5.nodeType!=dojo.html.ELEMENT_NODE){
continue;
}
var pos=dojo.html.getAbsolutePosition(_8a5,true);
var _8a7=dojo.html.getBorderBox(_8a5);
this.childBoxes.push({top:pos.y,bottom:pos.y+_8a7.height,left:pos.x,right:pos.x+_8a7.width,height:_8a7.height,width:_8a7.width,node:_8a5});
}
return true;
},_getNodeUnderMouse:function(e){
for(var i=0,_8aa;i<this.childBoxes.length;i++){
with(this.childBoxes[i]){
if(e.pageX>=left&&e.pageX<=right&&e.pageY>=top&&e.pageY<=bottom){
return i;
}
}
}
return -1;
},createDropIndicator:function(){
this.dropIndicator=document.createElement("div");
with(this.dropIndicator.style){
position="absolute";
zIndex=999;
if(this.vertical){
borderLeftWidth="1px";
borderLeftColor="black";
borderLeftStyle="solid";
height=dojo.html.getBorderBox(this.domNode).height+"px";
top=dojo.html.getAbsolutePosition(this.domNode,true).y+"px";
}else{
borderTopWidth="1px";
borderTopColor="black";
borderTopStyle="solid";
width=dojo.html.getBorderBox(this.domNode).width+"px";
left=dojo.html.getAbsolutePosition(this.domNode,true).x+"px";
}
}
},onDragMove:function(e,_8ac){
var i=this._getNodeUnderMouse(e);
if(!this.dropIndicator){
this.createDropIndicator();
}
var _8ae=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
var hide=false;
if(i<0){
if(this.childBoxes.length){
var _8b0=(dojo.html.gravity(this.childBoxes[0].node,e)&_8ae);
if(_8b0){
hide=true;
}
}else{
var _8b0=true;
}
}else{
var _8b1=this.childBoxes[i];
var _8b0=(dojo.html.gravity(_8b1.node,e)&_8ae);
if(_8b1.node===_8ac[0].dragSource.domNode){
hide=true;
}else{
var _8b2=_8b0?(i>0?this.childBoxes[i-1]:_8b1):(i<this.childBoxes.length-1?this.childBoxes[i+1]:_8b1);
if(_8b2.node===_8ac[0].dragSource.domNode){
hide=true;
}
}
}
if(hide){
this.dropIndicator.style.display="none";
return;
}else{
this.dropIndicator.style.display="";
}
this.placeIndicator(e,_8ac,i,_8b0);
if(!dojo.html.hasParent(this.dropIndicator)){
dojo.body().appendChild(this.dropIndicator);
}
},placeIndicator:function(e,_8b4,_8b5,_8b6){
var _8b7=this.vertical?"left":"top";
var _8b8;
if(_8b5<0){
if(this.childBoxes.length){
_8b8=_8b6?this.childBoxes[0]:this.childBoxes[this.childBoxes.length-1];
}else{
this.dropIndicator.style[_8b7]=dojo.html.getAbsolutePosition(this.domNode,true)[this.vertical?"x":"y"]+"px";
}
}else{
_8b8=this.childBoxes[_8b5];
}
if(_8b8){
this.dropIndicator.style[_8b7]=(_8b6?_8b8[_8b7]:_8b8[this.vertical?"right":"bottom"])+"px";
if(this.vertical){
this.dropIndicator.style.height=_8b8.height+"px";
this.dropIndicator.style.top=_8b8.top+"px";
}else{
this.dropIndicator.style.width=_8b8.width+"px";
this.dropIndicator.style.left=_8b8.left+"px";
}
}
},onDragOut:function(e){
if(this.dropIndicator){
dojo.html.removeNode(this.dropIndicator);
delete this.dropIndicator;
}
},onDrop:function(e){
this.onDragOut(e);
var i=this._getNodeUnderMouse(e);
var _8bc=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
if(i<0){
if(this.childBoxes.length){
if(dojo.html.gravity(this.childBoxes[0].node,e)&_8bc){
return this.insert(e,this.childBoxes[0].node,"before");
}else{
return this.insert(e,this.childBoxes[this.childBoxes.length-1].node,"after");
}
}
return this.insert(e,this.domNode,"append");
}
var _8bd=this.childBoxes[i];
if(dojo.html.gravity(_8bd.node,e)&_8bc){
return this.insert(e,_8bd.node,"before");
}else{
return this.insert(e,_8bd.node,"after");
}
},insert:function(e,_8bf,_8c0){
var node=e.dragObject.domNode;
if(_8c0=="before"){
return dojo.html.insertBefore(node,_8bf);
}else{
if(_8c0=="after"){
return dojo.html.insertAfter(node,_8bf);
}else{
if(_8c0=="append"){
_8bf.appendChild(node);
return true;
}
}
}
return false;
}});
dojo.provide("dojo.dnd.*");
dojo.provide("dojo.ns");
dojo.ns={namespaces:{},failed:{},loading:{},loaded:{},register:function(name,_8c3,_8c4,_8c5){
if(!_8c5||!this.namespaces[name]){
this.namespaces[name]=new dojo.ns.Ns(name,_8c3,_8c4);
}
},allow:function(name){
if(this.failed[name]){
return false;
}
if((djConfig.excludeNamespace)&&(dojo.lang.inArray(djConfig.excludeNamespace,name))){
return false;
}
return ((name==this.dojo)||(!djConfig.includeNamespace)||(dojo.lang.inArray(djConfig.includeNamespace,name)));
},get:function(name){
return this.namespaces[name];
},require:function(name){
var ns=this.namespaces[name];
if((ns)&&(this.loaded[name])){
return ns;
}
if(!this.allow(name)){
return false;
}
if(this.loading[name]){
dojo.debug("dojo.namespace.require: re-entrant request to load namespace \""+name+"\" must fail.");
return false;
}
var req=dojo.require;
this.loading[name]=true;
try{
if(name=="dojo"){
req("dojo.namespaces.dojo");
}else{
if(!dojo.hostenv.moduleHasPrefix(name)){
dojo.registerModulePath(name,"../"+name);
}
req([name,"manifest"].join("."),false,true);
}
if(!this.namespaces[name]){
this.failed[name]=true;
}
}
finally{
this.loading[name]=false;
}
return this.namespaces[name];
}};
dojo.ns.Ns=function(name,_8cc,_8cd){
this.name=name;
this.module=_8cc;
this.resolver=_8cd;
this._loaded=[];
this._failed=[];
};
dojo.ns.Ns.prototype.resolve=function(name,_8cf,_8d0){
if(!this.resolver||djConfig["skipAutoRequire"]){
return false;
}
var _8d1=this.resolver(name,_8cf);
if((_8d1)&&(!this._loaded[_8d1])&&(!this._failed[_8d1])){
var req=dojo.require;
req(_8d1,false,true);
if(dojo.hostenv.findModule(_8d1,false)){
this._loaded[_8d1]=true;
}else{
if(!_8d0){
dojo.raise("dojo.ns.Ns.resolve: module '"+_8d1+"' not found after loading via namespace '"+this.name+"'");
}
this._failed[_8d1]=true;
}
}
return Boolean(this._loaded[_8d1]);
};
dojo.registerNamespace=function(name,_8d4,_8d5){
dojo.ns.register.apply(dojo.ns,arguments);
};
dojo.registerNamespaceResolver=function(name,_8d7){
var n=dojo.ns.namespaces[name];
if(n){
n.resolver=_8d7;
}
};
dojo.registerNamespaceManifest=function(_8d9,path,name,_8dc,_8dd){
dojo.registerModulePath(name,path);
dojo.registerNamespace(name,_8dc,_8dd);
};
dojo.registerNamespace("dojo","dojo.widget");
dojo.provide("dojo.widget.Manager");
dojo.widget.manager=new function(){
this.widgets=[];
this.widgetIds=[];
this.topWidgets={};
var _8de={};
var _8df=[];
this.getUniqueId=function(_8e0){
var _8e1;
do{
_8e1=_8e0+"_"+(_8de[_8e0]!=undefined?++_8de[_8e0]:_8de[_8e0]=0);
}while(this.getWidgetById(_8e1));
return _8e1;
};
this.add=function(_8e2){
this.widgets.push(_8e2);
if(!_8e2.extraArgs["id"]){
_8e2.extraArgs["id"]=_8e2.extraArgs["ID"];
}
if(_8e2.widgetId==""){
if(_8e2["id"]){
_8e2.widgetId=_8e2["id"];
}else{
if(_8e2.extraArgs["id"]){
_8e2.widgetId=_8e2.extraArgs["id"];
}else{
_8e2.widgetId=this.getUniqueId(_8e2.ns+"_"+_8e2.widgetType);
}
}
}
if(this.widgetIds[_8e2.widgetId]){
dojo.debug("widget ID collision on ID: "+_8e2.widgetId);
}
this.widgetIds[_8e2.widgetId]=_8e2;
};
this.destroyAll=function(){
for(var x=this.widgets.length-1;x>=0;x--){
try{
this.widgets[x].destroy(true);
delete this.widgets[x];
}
catch(e){
}
}
};
this.remove=function(_8e4){
if(dojo.lang.isNumber(_8e4)){
var tw=this.widgets[_8e4].widgetId;
delete this.widgetIds[tw];
this.widgets.splice(_8e4,1);
}else{
this.removeById(_8e4);
}
};
this.removeById=function(id){
if(!dojo.lang.isString(id)){
id=id["widgetId"];
if(!id){
dojo.debug("invalid widget or id passed to removeById");
return;
}
}
for(var i=0;i<this.widgets.length;i++){
if(this.widgets[i].widgetId==id){
this.remove(i);
break;
}
}
};
this.getWidgetById=function(id){
if(dojo.lang.isString(id)){
return this.widgetIds[id];
}
return id;
};
this.getWidgetsByType=function(type){
var lt=type.toLowerCase();
var _8eb=(type.indexOf(":")<0?function(x){
return x.widgetType.toLowerCase();
}:function(x){
return x.getNamespacedType();
});
var ret=[];
dojo.lang.forEach(this.widgets,function(x){
if(_8eb(x)==lt){
ret.push(x);
}
});
return ret;
};
this.getWidgetsByFilter=function(_8f0,_8f1){
var ret=[];
dojo.lang.every(this.widgets,function(x){
if(_8f0(x)){
ret.push(x);
if(_8f1){
return false;
}
}
return true;
});
return (_8f1?ret[0]:ret);
};
this.getAllWidgets=function(){
return this.widgets.concat();
};
this.getWidgetByNode=function(node){
var w=this.getAllWidgets();
node=dojo.byId(node);
for(var i=0;i<w.length;i++){
if(w[i].domNode==node){
return w[i];
}
}
return null;
};
this.byId=this.getWidgetById;
this.byType=this.getWidgetsByType;
this.byFilter=this.getWidgetsByFilter;
this.byNode=this.getWidgetByNode;
var _8f7={};
var _8f8=["dojo.widget"];
for(var i=0;i<_8f8.length;i++){
_8f8[_8f8[i]]=true;
}
this.registerWidgetPackage=function(_8fa){
if(!_8f8[_8fa]){
_8f8[_8fa]=true;
_8f8.push(_8fa);
}
};
this.getWidgetPackageList=function(){
return dojo.lang.map(_8f8,function(elt){
return (elt!==true?elt:undefined);
});
};
this.getImplementation=function(_8fc,_8fd,_8fe,ns){
var impl=this.getImplementationName(_8fc,ns);
if(impl){
var ret=_8fd?new impl(_8fd):new impl();
return ret;
}
};
function buildPrefixCache(){
for(var _902 in dojo.render){
if(dojo.render[_902]["capable"]===true){
var _903=dojo.render[_902].prefixes;
for(var i=0;i<_903.length;i++){
_8df.push(_903[i].toLowerCase());
}
}
}
}
var _905=function(_906,_907){
if(!_907){
return null;
}
for(var i=0,l=_8df.length,_90a;i<=l;i++){
_90a=(i<l?_907[_8df[i]]:_907);
if(!_90a){
continue;
}
for(var name in _90a){
if(name.toLowerCase()==_906){
return _90a[name];
}
}
}
return null;
};
var _90c=function(_90d,_90e){
var _90f=dojo.evalObjPath(_90e,false);
return (_90f?_905(_90d,_90f):null);
};
this.getImplementationName=function(_910,ns){
var _912=_910.toLowerCase();
ns=ns||"dojo";
var imps=_8f7[ns]||(_8f7[ns]={});
var impl=imps[_912];
if(impl){
return impl;
}
if(!_8df.length){
buildPrefixCache();
}
var _915=dojo.ns.get(ns);
if(!_915){
dojo.ns.register(ns,ns+".widget");
_915=dojo.ns.get(ns);
}
if(_915){
_915.resolve(_910);
}
impl=_90c(_912,_915.module);
if(impl){
return (imps[_912]=impl);
}
_915=dojo.ns.require(ns);
if((_915)&&(_915.resolver)){
_915.resolve(_910);
impl=_90c(_912,_915.module);
if(impl){
return (imps[_912]=impl);
}
}
throw new Error("Could not locate widget implementation for \""+_910+"\" in \""+_915.module+"\" registered to namespace \""+_915.name+"\"");
};
this.resizing=false;
this.onWindowResized=function(){
if(this.resizing){
return;
}
try{
this.resizing=true;
for(var id in this.topWidgets){
var _917=this.topWidgets[id];
if(_917.checkSize){
_917.checkSize();
}
}
}
catch(e){
}
finally{
this.resizing=false;
}
};
if(typeof window!="undefined"){
dojo.addOnLoad(this,"onWindowResized");
dojo.event.connect(window,"onresize",this,"onWindowResized");
}
};
(function(){
var dw=dojo.widget;
var dwm=dw.manager;
var h=dojo.lang.curry(dojo.lang,"hitch",dwm);
var g=function(_91c,_91d){
dw[(_91d||_91c)]=h(_91c);
};
g("add","addWidget");
g("destroyAll","destroyAllWidgets");
g("remove","removeWidget");
g("removeById","removeWidgetById");
g("getWidgetById");
g("getWidgetById","byId");
g("getWidgetsByType");
g("getWidgetsByFilter");
g("getWidgetsByType","byType");
g("getWidgetsByFilter","byFilter");
g("getWidgetByNode","byNode");
dw.all=function(n){
var _91f=dwm.getAllWidgets.apply(dwm,arguments);
if(arguments.length>0){
return _91f[n];
}
return _91f;
};
g("registerWidgetPackage");
g("getImplementation","getWidgetImplementation");
g("getImplementationName","getWidgetImplementationName");
dw.widgets=dwm.widgets;
dw.widgetIds=dwm.widgetIds;
dw.root=dwm.root;
})();
dojo.provide("dojo.a11y");
dojo.a11y={imgPath:dojo.uri.dojoUri("src/widget/templates/images"),doAccessibleCheck:true,accessible:null,checkAccessible:function(){
if(this.accessible===null){
this.accessible=false;
if(this.doAccessibleCheck==true){
this.accessible=this.testAccessible();
}
}
return this.accessible;
},testAccessible:function(){
this.accessible=false;
if(dojo.render.html.ie||dojo.render.html.mozilla){
var div=document.createElement("div");
div.style.backgroundImage="url(\""+this.imgPath+"/tab_close.gif\")";
dojo.body().appendChild(div);
var _921=null;
if(window.getComputedStyle){
var _922=getComputedStyle(div,"");
_921=_922.getPropertyValue("background-image");
}else{
_921=div.currentStyle.backgroundImage;
}
var _923=false;
if(_921!=null&&(_921=="none"||_921=="url(invalid-url:)")){
this.accessible=true;
}
dojo.body().removeChild(div);
}
return this.accessible;
},setAccessible:function(_924){
this.accessible=_924;
},setCheckAccessible:function(_925){
this.doAccessibleCheck=_925;
},setAccessibleMode:function(){
if(this.accessible===null){
if(this.checkAccessible()){
dojo.render.html.prefixes.unshift("a11y");
}
}
return this.accessible;
}};
dojo.provide("dojo.widget.Widget");
dojo.declare("dojo.widget.Widget",null,function(){
this.children=[];
this.extraArgs={};
},{parent:null,isTopLevel:false,disabled:false,isContainer:false,widgetId:"",widgetType:"Widget",ns:"dojo",getNamespacedType:function(){
return (this.ns?this.ns+":"+this.widgetType:this.widgetType).toLowerCase();
},toString:function(){
return "[Widget "+this.getNamespacedType()+", "+(this.widgetId||"NO ID")+"]";
},repr:function(){
return this.toString();
},enable:function(){
this.disabled=false;
},disable:function(){
this.disabled=true;
},onResized:function(){
this.notifyChildrenOfResize();
},notifyChildrenOfResize:function(){
for(var i=0;i<this.children.length;i++){
var _927=this.children[i];
if(_927.onResized){
_927.onResized();
}
}
},create:function(args,_929,_92a,ns){
if(ns){
this.ns=ns;
}
this.satisfyPropertySets(args,_929,_92a);
this.mixInProperties(args,_929,_92a);
this.postMixInProperties(args,_929,_92a);
dojo.widget.manager.add(this);
this.buildRendering(args,_929,_92a);
this.initialize(args,_929,_92a);
this.postInitialize(args,_929,_92a);
this.postCreate(args,_929,_92a);
return this;
},destroy:function(_92c){
if(this.parent){
this.parent.removeChild(this);
}
this.destroyChildren();
this.uninitialize();
this.destroyRendering(_92c);
dojo.widget.manager.removeById(this.widgetId);
},destroyChildren:function(){
var _92d;
var i=0;
while(this.children.length>i){
_92d=this.children[i];
if(_92d instanceof dojo.widget.Widget){
this.removeChild(_92d);
_92d.destroy();
continue;
}
i++;
}
},getChildrenOfType:function(type,_930){
var ret=[];
var _932=dojo.lang.isFunction(type);
if(!_932){
type=type.toLowerCase();
}
for(var x=0;x<this.children.length;x++){
if(_932){
if(this.children[x] instanceof type){
ret.push(this.children[x]);
}
}else{
if(this.children[x].widgetType.toLowerCase()==type){
ret.push(this.children[x]);
}
}
if(_930){
ret=ret.concat(this.children[x].getChildrenOfType(type,_930));
}
}
return ret;
},getDescendants:function(){
var _934=[];
var _935=[this];
var elem;
while((elem=_935.pop())){
_934.push(elem);
if(elem.children){
dojo.lang.forEach(elem.children,function(elem){
_935.push(elem);
});
}
}
return _934;
},isFirstChild:function(){
return this===this.parent.children[0];
},isLastChild:function(){
return this===this.parent.children[this.parent.children.length-1];
},satisfyPropertySets:function(args){
return args;
},mixInProperties:function(args,frag){
if((args["fastMixIn"])||(frag["fastMixIn"])){
for(var x in args){
this[x]=args[x];
}
return;
}
var _93c;
var _93d=dojo.widget.lcArgsCache[this.widgetType];
if(_93d==null){
_93d={};
for(var y in this){
_93d[((new String(y)).toLowerCase())]=y;
}
dojo.widget.lcArgsCache[this.widgetType]=_93d;
}
var _93f={};
for(var x in args){
if(!this[x]){
var y=_93d[(new String(x)).toLowerCase()];
if(y){
args[y]=args[x];
x=y;
}
}
if(_93f[x]){
continue;
}
_93f[x]=true;
if((typeof this[x])!=(typeof _93c)){
if(typeof args[x]!="string"){
this[x]=args[x];
}else{
if(dojo.lang.isString(this[x])){
this[x]=args[x];
}else{
if(dojo.lang.isNumber(this[x])){
this[x]=new Number(args[x]);
}else{
if(dojo.lang.isBoolean(this[x])){
this[x]=(args[x].toLowerCase()=="false")?false:true;
}else{
if(dojo.lang.isFunction(this[x])){
if(args[x].search(/[^\w\.]+/i)==-1){
this[x]=dojo.evalObjPath(args[x],false);
}else{
var tn=dojo.lang.nameAnonFunc(new Function(args[x]),this);
dojo.event.kwConnect({srcObj:this,srcFunc:x,adviceObj:this,adviceFunc:tn});
}
}else{
if(dojo.lang.isArray(this[x])){
this[x]=args[x].split(";");
}else{
if(this[x] instanceof Date){
this[x]=new Date(Number(args[x]));
}else{
if(typeof this[x]=="object"){
if(this[x] instanceof dojo.uri.Uri){
this[x]=dojo.uri.dojoUri(args[x]);
}else{
var _941=args[x].split(";");
for(var y=0;y<_941.length;y++){
var si=_941[y].indexOf(":");
if((si!=-1)&&(_941[y].length>si)){
this[x][_941[y].substr(0,si).replace(/^\s+|\s+$/g,"")]=_941[y].substr(si+1);
}
}
}
}else{
this[x]=args[x];
}
}
}
}
}
}
}
}
}else{
this.extraArgs[x.toLowerCase()]=args[x];
}
}
},postMixInProperties:function(args,frag,_945){
},initialize:function(args,frag,_948){
return false;
},postInitialize:function(args,frag,_94b){
return false;
},postCreate:function(args,frag,_94e){
return false;
},uninitialize:function(){
return false;
},buildRendering:function(args,frag,_951){
dojo.unimplemented("dojo.widget.Widget.buildRendering, on "+this.toString()+", ");
return false;
},destroyRendering:function(){
dojo.unimplemented("dojo.widget.Widget.destroyRendering");
return false;
},addedTo:function(_952){
},addChild:function(_953){
dojo.unimplemented("dojo.widget.Widget.addChild");
return false;
},removeChild:function(_954){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_954){
this.children.splice(x,1);
_954.parent=null;
break;
}
}
return _954;
},getPreviousSibling:function(){
var idx=this.getParentIndex();
if(idx<=0){
return null;
}
return this.parent.children[idx-1];
},getSiblings:function(){
return this.parent.children;
},getParentIndex:function(){
return dojo.lang.indexOf(this.parent.children,this,true);
},getNextSibling:function(){
var idx=this.getParentIndex();
if(idx==this.parent.children.length-1){
return null;
}
if(idx<0){
return null;
}
return this.parent.children[idx+1];
}});
dojo.widget.lcArgsCache={};
dojo.widget.tags={};
dojo.widget.tags["dojo:propertyset"]=function(_958,_959,_95a){
var _95b=_959.parseProperties(_958["dojo:propertyset"]);
};
dojo.widget.tags["dojo:connect"]=function(_95c,_95d,_95e){
var _95f=_95d.parseProperties(_95c["dojo:connect"]);
};
dojo.widget.buildWidgetFromParseTree=function(type,frag,_962,_963,_964,_965){
dojo.a11y.setAccessibleMode();
var _966=type.split(":");
_966=(_966.length==2)?_966[1]:type;
var _967=_965||_962.parseProperties(frag[frag["ns"]+":"+_966]);
var _968=dojo.widget.manager.getImplementation(_966,null,null,frag["ns"]);
if(!_968){
throw new Error("cannot find \""+type+"\" widget");
}else{
if(!_968.create){
throw new Error("\""+type+"\" widget object has no \"create\" method and does not appear to implement *Widget");
}
}
_967["dojoinsertionindex"]=_964;
var ret=_968.create(_967,frag,_963,frag["ns"]);
return ret;
};
dojo.widget.defineWidget=function(_96a,_96b,_96c,init,_96e){
if(dojo.lang.isString(arguments[3])){
dojo.widget._defineWidget(arguments[0],arguments[3],arguments[1],arguments[4],arguments[2]);
}else{
var args=[arguments[0]],p=3;
if(dojo.lang.isString(arguments[1])){
args.push(arguments[1],arguments[2]);
}else{
args.push("",arguments[1]);
p=2;
}
if(dojo.lang.isFunction(arguments[p])){
args.push(arguments[p],arguments[p+1]);
}else{
args.push(null,arguments[p]);
}
dojo.widget._defineWidget.apply(this,args);
}
};
dojo.widget.defineWidget.renderers="html|svg|vml";
dojo.widget._defineWidget=function(_971,_972,_973,init,_975){
var _976=_971.split(".");
var type=_976.pop();
var regx="\\.("+(_972?_972+"|":"")+dojo.widget.defineWidget.renderers+")\\.";
var r=_971.search(new RegExp(regx));
_976=(r<0?_976.join("."):_971.substr(0,r));
dojo.widget.manager.registerWidgetPackage(_976);
var pos=_976.indexOf(".");
var _97b=(pos>-1)?_976.substring(0,pos):_976;
_975=(_975)||{};
_975.widgetType=type;
if((!init)&&(_975["classConstructor"])){
init=_975.classConstructor;
delete _975.classConstructor;
}
dojo.declare(_971,_973,init,_975);
};
dojo.provide("dojo.widget.Parse");
dojo.widget.Parse=function(_97c){
this.propertySetsList=[];
this.fragment=_97c;
this.createComponents=function(frag,_97e){
var _97f=[];
var _980=false;
try{
if(frag&&frag.tagName&&(frag!=frag.nodeRef)){
var _981=dojo.widget.tags;
var tna=String(frag.tagName).split(";");
for(var x=0;x<tna.length;x++){
var ltn=tna[x].replace(/^\s+|\s+$/g,"").toLowerCase();
frag.tagName=ltn;
var ret;
if(_981[ltn]){
_980=true;
ret=_981[ltn](frag,this,_97e,frag.index);
_97f.push(ret);
}else{
if(ltn.indexOf(":")==-1){
ltn="dojo:"+ltn;
}
ret=dojo.widget.buildWidgetFromParseTree(ltn,frag,this,_97e,frag.index);
if(ret){
_980=true;
_97f.push(ret);
}
}
}
}
}
catch(e){
dojo.debug("dojo.widget.Parse: error:",e);
}
if(!_980){
_97f=_97f.concat(this.createSubComponents(frag,_97e));
}
return _97f;
};
this.createSubComponents=function(_986,_987){
var frag,_989=[];
for(var item in _986){
frag=_986[item];
if(frag&&typeof frag=="object"&&(frag!=_986.nodeRef)&&(frag!=_986.tagName)&&(item.indexOf("$")==-1)){
_989=_989.concat(this.createComponents(frag,_987));
}
}
return _989;
};
this.parsePropertySets=function(_98b){
return [];
};
this.parseProperties=function(_98c){
var _98d={};
for(var item in _98c){
if((_98c[item]==_98c.tagName)||(_98c[item]==_98c.nodeRef)){
}else{
var frag=_98c[item];
if(frag.tagName&&dojo.widget.tags[frag.tagName.toLowerCase()]){
}else{
if(frag[0]&&frag[0].value!=""&&frag[0].value!=null){
try{
if(item.toLowerCase()=="dataprovider"){
var _990=this;
this.getDataProvider(_990,frag[0].value);
_98d.dataProvider=this.dataProvider;
}
_98d[item]=frag[0].value;
var _991=this.parseProperties(frag);
for(var _992 in _991){
_98d[_992]=_991[_992];
}
}
catch(e){
dojo.debug(e);
}
}
}
switch(item.toLowerCase()){
case "checked":
case "disabled":
if(typeof _98d[item]!="boolean"){
_98d[item]=true;
}
break;
}
}
}
return _98d;
};
this.getDataProvider=function(_993,_994){
dojo.io.bind({url:_994,load:function(type,_996){
if(type=="load"){
_993.dataProvider=_996;
}
},mimetype:"text/javascript",sync:true});
};
this.getPropertySetById=function(_997){
for(var x=0;x<this.propertySetsList.length;x++){
if(_997==this.propertySetsList[x]["id"][0].value){
return this.propertySetsList[x];
}
}
return "";
};
this.getPropertySetsByType=function(_999){
var _99a=[];
for(var x=0;x<this.propertySetsList.length;x++){
var cpl=this.propertySetsList[x];
var cpcc=cpl.componentClass||cpl.componentType||null;
var _99e=this.propertySetsList[x]["id"][0].value;
if(cpcc&&(_99e==cpcc[0].value)){
_99a.push(cpl);
}
}
return _99a;
};
this.getPropertySets=function(_99f){
var ppl="dojo:propertyproviderlist";
var _9a1=[];
var _9a2=_99f.tagName;
if(_99f[ppl]){
var _9a3=_99f[ppl].value.split(" ");
for(var _9a4 in _9a3){
if((_9a4.indexOf("..")==-1)&&(_9a4.indexOf("://")==-1)){
var _9a5=this.getPropertySetById(_9a4);
if(_9a5!=""){
_9a1.push(_9a5);
}
}else{
}
}
}
return this.getPropertySetsByType(_9a2).concat(_9a1);
};
this.createComponentFromScript=function(_9a6,_9a7,_9a8,ns){
_9a8.fastMixIn=true;
var ltn=(ns||"dojo")+":"+_9a7.toLowerCase();
if(dojo.widget.tags[ltn]){
return [dojo.widget.tags[ltn](_9a8,this,null,null,_9a8)];
}
return [dojo.widget.buildWidgetFromParseTree(ltn,_9a8,this,null,null,_9a8)];
};
};
dojo.widget._parser_collection={"dojo":new dojo.widget.Parse()};
dojo.widget.getParser=function(name){
if(!name){
name="dojo";
}
if(!this._parser_collection[name]){
this._parser_collection[name]=new dojo.widget.Parse();
}
return this._parser_collection[name];
};
dojo.widget.createWidget=function(name,_9ad,_9ae,_9af){
var _9b0=false;
var _9b1=(typeof name=="string");
if(_9b1){
var pos=name.indexOf(":");
var ns=(pos>-1)?name.substring(0,pos):"dojo";
if(pos>-1){
name=name.substring(pos+1);
}
var _9b4=name.toLowerCase();
var _9b5=ns+":"+_9b4;
_9b0=(dojo.byId(name)&&!dojo.widget.tags[_9b5]);
}
if((arguments.length==1)&&(_9b0||!_9b1)){
var xp=new dojo.xml.Parse();
var tn=_9b0?dojo.byId(name):name;
return dojo.widget.getParser().createComponents(xp.parseElement(tn,null,true))[0];
}
function fromScript(_9b8,name,_9ba,ns){
_9ba[_9b5]={dojotype:[{value:_9b4}],nodeRef:_9b8,fastMixIn:true};
_9ba.ns=ns;
return dojo.widget.getParser().createComponentFromScript(_9b8,name,_9ba,ns);
}
_9ad=_9ad||{};
var _9bc=false;
var tn=null;
var h=dojo.render.html.capable;
if(h){
tn=document.createElement("span");
}
if(!_9ae){
_9bc=true;
_9ae=tn;
if(h){
dojo.body().appendChild(_9ae);
}
}else{
if(_9af){
dojo.dom.insertAtPosition(tn,_9ae,_9af);
}else{
tn=_9ae;
}
}
var _9be=fromScript(tn,name.toLowerCase(),_9ad,ns);
if((!_9be)||(!_9be[0])||(typeof _9be[0].widgetType=="undefined")){
throw new Error("createWidget: Creation of \""+name+"\" widget failed.");
}
try{
if(_9bc&&_9be[0].domNode.parentNode){
_9be[0].domNode.parentNode.removeChild(_9be[0].domNode);
}
}
catch(e){
dojo.debug(e);
}
return _9be[0];
};
dojo.provide("dojo.widget.DomWidget");
dojo.widget._cssFiles={};
dojo.widget._cssStrings={};
dojo.widget._templateCache={};
dojo.widget.defaultStrings={dojoRoot:dojo.hostenv.getBaseScriptUri(),baseScriptUri:dojo.hostenv.getBaseScriptUri()};
dojo.widget.fillFromTemplateCache=function(obj,_9c0,_9c1,_9c2){
var _9c3=_9c0||obj.templatePath;
var _9c4=dojo.widget._templateCache;
if(!_9c3&&!obj["widgetType"]){
do{
var _9c5="__dummyTemplate__"+dojo.widget._templateCache.dummyCount++;
}while(_9c4[_9c5]);
obj.widgetType=_9c5;
}
var wt=_9c3?_9c3.toString():obj.widgetType;
var ts=_9c4[wt];
if(!ts){
_9c4[wt]={"string":null,"node":null};
if(_9c2){
ts={};
}else{
ts=_9c4[wt];
}
}
if((!obj.templateString)&&(!_9c2)){
obj.templateString=_9c1||ts["string"];
}
if((!obj.templateNode)&&(!_9c2)){
obj.templateNode=ts["node"];
}
if((!obj.templateNode)&&(!obj.templateString)&&(_9c3)){
var _9c8=dojo.hostenv.getText(_9c3);
if(_9c8){
_9c8=_9c8.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _9c9=_9c8.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_9c9){
_9c8=_9c9[1];
}
}else{
_9c8="";
}
obj.templateString=_9c8;
if(!_9c2){
_9c4[wt]["string"]=_9c8;
}
}
if((!ts["string"])&&(!_9c2)){
ts.string=obj.templateString;
}
};
dojo.widget._templateCache.dummyCount=0;
dojo.widget.attachProperties=["dojoAttachPoint","id"];
dojo.widget.eventAttachProperty="dojoAttachEvent";
dojo.widget.onBuildProperty="dojoOnBuild";
dojo.widget.waiNames=["waiRole","waiState"];
dojo.widget.wai={waiRole:{name:"waiRole","namespace":"http://www.w3.org/TR/xhtml2",alias:"x2",prefix:"wairole:"},waiState:{name:"waiState","namespace":"http://www.w3.org/2005/07/aaa",alias:"aaa",prefix:""},setAttr:function(node,ns,attr,_9cd){
if(dojo.render.html.ie){
node.setAttribute(this[ns].alias+":"+attr,this[ns].prefix+_9cd);
}else{
node.setAttributeNS(this[ns]["namespace"],attr,this[ns].prefix+_9cd);
}
},getAttr:function(node,ns,attr){
if(dojo.render.html.ie){
return node.getAttribute(this[ns].alias+":"+attr);
}else{
return node.getAttributeNS(this[ns]["namespace"],attr);
}
},removeAttr:function(node,ns,attr){
var _9d4=true;
if(dojo.render.html.ie){
_9d4=node.removeAttribute(this[ns].alias+":"+attr);
}else{
node.removeAttributeNS(this[ns]["namespace"],attr);
}
return _9d4;
}};
dojo.widget.attachTemplateNodes=function(_9d5,_9d6,_9d7){
var _9d8=dojo.dom.ELEMENT_NODE;
function trim(str){
return str.replace(/^\s+|\s+$/g,"");
}
if(!_9d5){
_9d5=_9d6.domNode;
}
if(_9d5.nodeType!=_9d8){
return;
}
var _9da=_9d5.all||_9d5.getElementsByTagName("*");
var _9db=_9d6;
for(var x=-1;x<_9da.length;x++){
var _9dd=(x==-1)?_9d5:_9da[x];
var _9de=[];
if(!_9d6.widgetsInTemplate||!_9dd.getAttribute("dojoType")){
for(var y=0;y<this.attachProperties.length;y++){
var _9e0=_9dd.getAttribute(this.attachProperties[y]);
if(_9e0){
_9de=_9e0.split(";");
for(var z=0;z<_9de.length;z++){
if(dojo.lang.isArray(_9d6[_9de[z]])){
_9d6[_9de[z]].push(_9dd);
}else{
_9d6[_9de[z]]=_9dd;
}
}
break;
}
}
var _9e2=_9dd.getAttribute(this.eventAttachProperty);
if(_9e2){
var evts=_9e2.split(";");
for(var y=0;y<evts.length;y++){
if((!evts[y])||(!evts[y].length)){
continue;
}
var _9e4=null;
var tevt=trim(evts[y]);
if(evts[y].indexOf(":")>=0){
var _9e6=tevt.split(":");
tevt=trim(_9e6[0]);
_9e4=trim(_9e6[1]);
}
if(!_9e4){
_9e4=tevt;
}
var tf=function(){
var ntf=new String(_9e4);
return function(evt){
if(_9db[ntf]){
_9db[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_9dd,tevt,tf,false,true);
}
}
for(var y=0;y<_9d7.length;y++){
var _9ea=_9dd.getAttribute(_9d7[y]);
if((_9ea)&&(_9ea.length)){
var _9e4=null;
var _9eb=_9d7[y].substr(4);
_9e4=trim(_9ea);
var _9ec=[_9e4];
if(_9e4.indexOf(";")>=0){
_9ec=dojo.lang.map(_9e4.split(";"),trim);
}
for(var z=0;z<_9ec.length;z++){
if(!_9ec[z].length){
continue;
}
var tf=function(){
var ntf=new String(_9ec[z]);
return function(evt){
if(_9db[ntf]){
_9db[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_9dd,_9eb,tf,false,true);
}
}
}
}
var _9ef=_9dd.getAttribute(this.templateProperty);
if(_9ef){
_9d6[_9ef]=_9dd;
}
dojo.lang.forEach(dojo.widget.waiNames,function(name){
var wai=dojo.widget.wai[name];
var val=_9dd.getAttribute(wai.name);
if(val){
if(val.indexOf("-")==-1){
dojo.widget.wai.setAttr(_9dd,wai.name,"role",val);
}else{
var _9f3=val.split("-");
dojo.widget.wai.setAttr(_9dd,wai.name,_9f3[0],_9f3[1]);
}
}
},this);
var _9f4=_9dd.getAttribute(this.onBuildProperty);
if(_9f4){
eval("var node = baseNode; var widget = targetObj; "+_9f4);
}
}
};
dojo.widget.getDojoEventsFromStr=function(str){
var re=/(dojoOn([a-z]+)(\s?))=/gi;
var evts=str?str.match(re)||[]:[];
var ret=[];
var lem={};
for(var x=0;x<evts.length;x++){
if(evts[x].length<1){
continue;
}
var cm=evts[x].replace(/\s/,"");
cm=(cm.slice(0,cm.length-1));
if(!lem[cm]){
lem[cm]=true;
ret.push(cm);
}
}
return ret;
};
dojo.declare("dojo.widget.DomWidget",dojo.widget.Widget,function(){
if((arguments.length>0)&&(typeof arguments[0]=="object")){
this.create(arguments[0]);
}
},{templateNode:null,templateString:null,templateCssString:null,preventClobber:false,domNode:null,containerNode:null,widgetsInTemplate:false,addChild:function(_9fc,_9fd,pos,ref,_a00){
if(!this.isContainer){
dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
return null;
}else{
if(_a00==undefined){
_a00=this.children.length;
}
this.addWidgetAsDirectChild(_9fc,_9fd,pos,ref,_a00);
this.registerChild(_9fc,_a00);
}
return _9fc;
},addWidgetAsDirectChild:function(_a01,_a02,pos,ref,_a05){
if((!this.containerNode)&&(!_a02)){
this.containerNode=this.domNode;
}
var cn=(_a02)?_a02:this.containerNode;
if(!pos){
pos="after";
}
if(!ref){
if(!cn){
cn=dojo.body();
}
ref=cn.lastChild;
}
if(!_a05){
_a05=0;
}
_a01.domNode.setAttribute("dojoinsertionindex",_a05);
if(!ref){
cn.appendChild(_a01.domNode);
}else{
if(pos=="insertAtIndex"){
dojo.dom.insertAtIndex(_a01.domNode,ref.parentNode,_a05);
}else{
if((pos=="after")&&(ref===cn.lastChild)){
cn.appendChild(_a01.domNode);
}else{
dojo.dom.insertAtPosition(_a01.domNode,cn,pos);
}
}
}
},registerChild:function(_a07,_a08){
_a07.dojoInsertionIndex=_a08;
var idx=-1;
for(var i=0;i<this.children.length;i++){
if(this.children[i].dojoInsertionIndex<=_a08){
idx=i;
}
}
this.children.splice(idx+1,0,_a07);
_a07.parent=this;
_a07.addedTo(this,idx+1);
delete dojo.widget.manager.topWidgets[_a07.widgetId];
},removeChild:function(_a0b){
dojo.dom.removeNode(_a0b.domNode);
return dojo.widget.DomWidget.superclass.removeChild.call(this,_a0b);
},getFragNodeRef:function(frag){
if(!frag){
return null;
}
if(!frag[this.getNamespacedType()]){
dojo.raise("Error: no frag for widget type "+this.getNamespacedType()+", id "+this.widgetId+" (maybe a widget has set it's type incorrectly)");
}
return frag[this.getNamespacedType()]["nodeRef"];
},postInitialize:function(args,frag,_a0f){
var _a10=this.getFragNodeRef(frag);
if(_a0f&&(_a0f.snarfChildDomOutput||!_a10)){
_a0f.addWidgetAsDirectChild(this,"","insertAtIndex","",args["dojoinsertionindex"],_a10);
}else{
if(_a10){
if(this.domNode&&(this.domNode!==_a10)){
this._sourceNodeRef=dojo.dom.replaceNode(_a10,this.domNode);
}
}
}
if(_a0f){
_a0f.registerChild(this,args.dojoinsertionindex);
}else{
dojo.widget.manager.topWidgets[this.widgetId]=this;
}
if(this.widgetsInTemplate){
var _a11=new dojo.xml.Parse();
var _a12;
var _a13=this.domNode.getElementsByTagName("*");
for(var i=0;i<_a13.length;i++){
if(_a13[i].getAttribute("dojoAttachPoint")=="subContainerWidget"){
_a12=_a13[i];
}
if(_a13[i].getAttribute("dojoType")){
_a13[i].setAttribute("isSubWidget",true);
}
}
if(this.isContainer&&!this.containerNode){
if(_a12){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,_a12);
frag["dojoDontFollow"]=true;
}
}else{
dojo.debug("No subContainerWidget node can be found in template file for widget "+this);
}
}
var _a16=_a11.parseElement(this.domNode,null,true);
dojo.widget.getParser().createSubComponents(_a16,this);
var _a17=[];
var _a18=[this];
var w;
while((w=_a18.pop())){
for(var i=0;i<w.children.length;i++){
var _a1a=w.children[i];
if(_a1a._processedSubWidgets||!_a1a.extraArgs["issubwidget"]){
continue;
}
_a17.push(_a1a);
if(_a1a.isContainer){
_a18.push(_a1a);
}
}
}
for(var i=0;i<_a17.length;i++){
var _a1b=_a17[i];
if(_a1b._processedSubWidgets){
dojo.debug("This should not happen: widget._processedSubWidgets is already true!");
return;
}
_a1b._processedSubWidgets=true;
if(_a1b.extraArgs["dojoattachevent"]){
var evts=_a1b.extraArgs["dojoattachevent"].split(";");
for(var j=0;j<evts.length;j++){
var _a1e=null;
var tevt=dojo.string.trim(evts[j]);
if(tevt.indexOf(":")>=0){
var _a20=tevt.split(":");
tevt=dojo.string.trim(_a20[0]);
_a1e=dojo.string.trim(_a20[1]);
}
if(!_a1e){
_a1e=tevt;
}
if(dojo.lang.isFunction(_a1b[tevt])){
dojo.event.kwConnect({srcObj:_a1b,srcFunc:tevt,targetObj:this,targetFunc:_a1e});
}else{
alert(tevt+" is not a function in widget "+_a1b);
}
}
}
if(_a1b.extraArgs["dojoattachpoint"]){
this[_a1b.extraArgs["dojoattachpoint"]]=_a1b;
}
}
}
if(this.isContainer&&!frag["dojoDontFollow"]){
dojo.widget.getParser().createSubComponents(frag,this);
}
},buildRendering:function(args,frag){
var ts=dojo.widget._templateCache[this.widgetType];
if(args["templatecsspath"]){
args["templateCssPath"]=args["templatecsspath"];
}
var _a24=args["templateCssPath"]||this.templateCssPath;
if(_a24&&!dojo.widget._cssFiles[_a24.toString()]){
if((!this.templateCssString)&&(_a24)){
this.templateCssString=dojo.hostenv.getText(_a24);
this.templateCssPath=null;
}
dojo.widget._cssFiles[_a24.toString()]=true;
}
if((this["templateCssString"])&&(!dojo.widget._cssStrings[this.templateCssString])){
dojo.html.insertCssText(this.templateCssString,null,_a24);
dojo.widget._cssStrings[this.templateCssString]=true;
}
if((!this.preventClobber)&&((this.templatePath)||(this.templateNode)||((this["templateString"])&&(this.templateString.length))||((typeof ts!="undefined")&&((ts["string"])||(ts["node"]))))){
this.buildFromTemplate(args,frag);
}else{
this.domNode=this.getFragNodeRef(frag);
}
this.fillInTemplate(args,frag);
},buildFromTemplate:function(args,frag){
var _a27=false;
if(args["templatepath"]){
args["templatePath"]=args["templatepath"];
}
dojo.widget.fillFromTemplateCache(this,args["templatePath"],null,_a27);
var ts=dojo.widget._templateCache[this.templatePath?this.templatePath.toString():this.widgetType];
if((ts)&&(!_a27)){
if(!this.templateString.length){
this.templateString=ts["string"];
}
if(!this.templateNode){
this.templateNode=ts["node"];
}
}
var _a29=false;
var node=null;
var tstr=this.templateString;
if((!this.templateNode)&&(this.templateString)){
_a29=this.templateString.match(/\$\{([^\}]+)\}/g);
if(_a29){
var hash=this.strings||{};
for(var key in dojo.widget.defaultStrings){
if(dojo.lang.isUndefined(hash[key])){
hash[key]=dojo.widget.defaultStrings[key];
}
}
for(var i=0;i<_a29.length;i++){
var key=_a29[i];
key=key.substring(2,key.length-1);
var kval=(key.substring(0,5)=="this.")?dojo.lang.getObjPathValue(key.substring(5),this):hash[key];
var _a30;
if((kval)||(dojo.lang.isString(kval))){
_a30=new String((dojo.lang.isFunction(kval))?kval.call(this,key,this.templateString):kval);
while(_a30.indexOf("\"")>-1){
_a30=_a30.replace("\"","&quot;");
}
tstr=tstr.replace(_a29[i],_a30);
}
}
}else{
this.templateNode=this.createNodesFromText(this.templateString,true)[0];
if(!_a27){
ts.node=this.templateNode;
}
}
}
if((!this.templateNode)&&(!_a29)){
dojo.debug("DomWidget.buildFromTemplate: could not create template");
return false;
}else{
if(!_a29){
node=this.templateNode.cloneNode(true);
if(!node){
return false;
}
}else{
node=this.createNodesFromText(tstr,true)[0];
}
}
this.domNode=node;
this.attachTemplateNodes();
if(this.isContainer&&this.containerNode){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,this.containerNode);
}
}
},attachTemplateNodes:function(_a32,_a33){
if(!_a32){
_a32=this.domNode;
}
if(!_a33){
_a33=this;
}
return dojo.widget.attachTemplateNodes(_a32,_a33,dojo.widget.getDojoEventsFromStr(this.templateString));
},fillInTemplate:function(){
},destroyRendering:function(){
try{
dojo.dom.destroyNode(this.domNode);
delete this.domNode;
}
catch(e){
}
if(this._sourceNodeRef){
try{
dojo.dom.destroyNode(this._sourceNodeRef);
}
catch(e){
}
}
},createNodesFromText:function(){
dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
}});
dojo.provide("dojo.lfx.toggle");
dojo.lfx.toggle.plain={show:function(node,_a35,_a36,_a37){
dojo.html.show(node);
if(dojo.lang.isFunction(_a37)){
_a37();
}
},hide:function(node,_a39,_a3a,_a3b){
dojo.html.hide(node);
if(dojo.lang.isFunction(_a3b)){
_a3b();
}
}};
dojo.lfx.toggle.fade={anim:null,show:function(node,_a3d,_a3e,_a3f){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.fadeShow(node,_a3d,_a3e,_a3f).play();
},hide:function(node,_a41,_a42,_a43){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.fadeHide(node,_a41,_a42,_a43).play();
}};
dojo.lfx.toggle.wipe={anim:null,show:function(node,_a45,_a46,_a47){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.wipeIn(node,_a45,_a46,_a47).play();
},hide:function(node,_a49,_a4a,_a4b){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.wipeOut(node,_a49,_a4a,_a4b).play();
}};
dojo.lfx.toggle.explode={anim:null,show:function(node,_a4d,_a4e,_a4f,_a50){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.explode(_a50||{x:0,y:0,width:0,height:0},node,_a4d,_a4e,_a4f).play();
},hide:function(node,_a52,_a53,_a54,_a55){
if(this.anim&&this.anim.status()!="stopped"){
this.anim.stop();
}
this.anim=dojo.lfx.implode(node,_a55||{x:0,y:0,width:0,height:0},_a52,_a53,_a54).play();
}};
dojo.provide("dojo.widget.HtmlWidget");
dojo.declare("dojo.widget.HtmlWidget",dojo.widget.DomWidget,{templateCssPath:null,templatePath:null,lang:"",toggle:"plain",toggleDuration:150,initialize:function(args,frag){
},postMixInProperties:function(args,frag){
if(this.lang===""){
this.lang=null;
}
this.toggleObj=dojo.lang.shallowCopy(dojo.lfx.toggle[this.toggle.toLowerCase()]||dojo.lfx.toggle.plain);
},createNodesFromText:function(txt,wrap){
return dojo.html.createNodesFromText(txt,wrap);
},destroyRendering:function(_a5c){
try{
if(this.bgIframe){
this.bgIframe.remove();
delete this.bgIframe;
}
if(!_a5c&&this.domNode){
dojo.event.browser.clean(this.domNode);
}
dojo.widget.HtmlWidget.superclass.destroyRendering.call(this);
}
catch(e){
}
},isShowing:function(){
return dojo.html.isShowing(this.domNode);
},toggleShowing:function(){
if(this.isShowing()){
this.hide();
}else{
this.show();
}
},show:function(){
if(this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.show(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onShow),this.explodeSrc);
},onShow:function(){
this.animationInProgress=false;
this.checkSize();
},hide:function(){
if(!this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.hide(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onHide),this.explodeSrc);
},onHide:function(){
this.animationInProgress=false;
},_isResized:function(w,h){
if(!this.isShowing()){
return false;
}
var wh=dojo.html.getMarginBox(this.domNode);
var _a60=w||wh.width;
var _a61=h||wh.height;
if(this.width==_a60&&this.height==_a61){
return false;
}
this.width=_a60;
this.height=_a61;
return true;
},checkSize:function(){
if(!this._isResized()){
return;
}
this.onResized();
},resizeTo:function(w,h){
dojo.html.setMarginBox(this.domNode,{width:w,height:h});
if(this.isShowing()){
this.onResized();
}
},resizeSoon:function(){
if(this.isShowing()){
dojo.lang.setTimeout(this,this.onResized,0);
}
},onResized:function(){
dojo.lang.forEach(this.children,function(_a64){
if(_a64.checkSize){
_a64.checkSize();
}
});
}});
dojo.provide("dojo.widget.*");
dojo.provide("dojo.math");
dojo.math.degToRad=function(x){
return (x*Math.PI)/180;
};
dojo.math.radToDeg=function(x){
return (x*180)/Math.PI;
};
dojo.math.factorial=function(n){
if(n<1){
return 0;
}
var _a68=1;
for(var i=1;i<=n;i++){
_a68*=i;
}
return _a68;
};
dojo.math.permutations=function(n,k){
if(n==0||k==0){
return 1;
}
return (dojo.math.factorial(n)/dojo.math.factorial(n-k));
};
dojo.math.combinations=function(n,r){
if(n==0||r==0){
return 1;
}
return (dojo.math.factorial(n)/(dojo.math.factorial(n-r)*dojo.math.factorial(r)));
};
dojo.math.bernstein=function(t,n,i){
return (dojo.math.combinations(n,i)*Math.pow(t,i)*Math.pow(1-t,n-i));
};
dojo.math.gaussianRandom=function(){
var k=2;
do{
var i=2*Math.random()-1;
var j=2*Math.random()-1;
k=i*i+j*j;
}while(k>=1);
k=Math.sqrt((-2*Math.log(k))/k);
return i*k;
};
dojo.math.mean=function(){
var _a74=dojo.lang.isArray(arguments[0])?arguments[0]:arguments;
var mean=0;
for(var i=0;i<_a74.length;i++){
mean+=_a74[i];
}
return mean/_a74.length;
};
dojo.math.round=function(_a77,_a78){
if(!_a78){
var _a79=1;
}else{
var _a79=Math.pow(10,_a78);
}
return Math.round(_a77*_a79)/_a79;
};
dojo.math.sd=dojo.math.standardDeviation=function(){
var _a7a=dojo.lang.isArray(arguments[0])?arguments[0]:arguments;
return Math.sqrt(dojo.math.variance(_a7a));
};
dojo.math.variance=function(){
var _a7b=dojo.lang.isArray(arguments[0])?arguments[0]:arguments;
var mean=0,_a7d=0;
for(var i=0;i<_a7b.length;i++){
mean+=_a7b[i];
_a7d+=Math.pow(_a7b[i],2);
}
return (_a7d/_a7b.length)-Math.pow(mean/_a7b.length,2);
};
dojo.math.range=function(a,b,step){
if(arguments.length<2){
b=a;
a=0;
}
if(arguments.length<3){
step=1;
}
var _a82=[];
if(step>0){
for(var i=a;i<b;i+=step){
_a82.push(i);
}
}else{
if(step<0){
for(var i=a;i>b;i+=step){
_a82.push(i);
}
}else{
throw new Error("dojo.math.range: step must be non-zero");
}
}
return _a82;
};
dojo.provide("dojo.math.curves");
dojo.math.curves={Line:function(_a84,end){
this.start=_a84;
this.end=end;
this.dimensions=_a84.length;
for(var i=0;i<_a84.length;i++){
_a84[i]=Number(_a84[i]);
}
for(var i=0;i<end.length;i++){
end[i]=Number(end[i]);
}
this.getValue=function(n){
var _a88=new Array(this.dimensions);
for(var i=0;i<this.dimensions;i++){
_a88[i]=((this.end[i]-this.start[i])*n)+this.start[i];
}
return _a88;
};
return this;
},Bezier:function(pnts){
this.getValue=function(step){
if(step>=1){
return this.p[this.p.length-1];
}
if(step<=0){
return this.p[0];
}
var _a8c=new Array(this.p[0].length);
for(var k=0;j<this.p[0].length;k++){
_a8c[k]=0;
}
for(var j=0;j<this.p[0].length;j++){
var C=0;
var D=0;
for(var i=0;i<this.p.length;i++){
C+=this.p[i][j]*this.p[this.p.length-1][0]*dojo.math.bernstein(step,this.p.length,i);
}
for(var l=0;l<this.p.length;l++){
D+=this.p[this.p.length-1][0]*dojo.math.bernstein(step,this.p.length,l);
}
_a8c[j]=C/D;
}
return _a8c;
};
this.p=pnts;
return this;
},CatmullRom:function(pnts,c){
this.getValue=function(step){
var _a96=step*(this.p.length-1);
var node=Math.floor(_a96);
var _a98=_a96-node;
var i0=node-1;
if(i0<0){
i0=0;
}
var i=node;
var i1=node+1;
if(i1>=this.p.length){
i1=this.p.length-1;
}
var i2=node+2;
if(i2>=this.p.length){
i2=this.p.length-1;
}
var u=_a98;
var u2=_a98*_a98;
var u3=_a98*_a98*_a98;
var _aa0=new Array(this.p[0].length);
for(var k=0;k<this.p[0].length;k++){
var x1=(-this.c*this.p[i0][k])+((2-this.c)*this.p[i][k])+((this.c-2)*this.p[i1][k])+(this.c*this.p[i2][k]);
var x2=(2*this.c*this.p[i0][k])+((this.c-3)*this.p[i][k])+((3-2*this.c)*this.p[i1][k])+(-this.c*this.p[i2][k]);
var x3=(-this.c*this.p[i0][k])+(this.c*this.p[i1][k]);
var x4=this.p[i][k];
_aa0[k]=x1*u3+x2*u2+x3*u+x4;
}
return _aa0;
};
if(!c){
this.c=0.7;
}else{
this.c=c;
}
this.p=pnts;
return this;
},Arc:function(_aa6,end,ccw){
var _aa9=dojo.math.points.midpoint(_aa6,end);
var _aaa=dojo.math.points.translate(dojo.math.points.invert(_aa9),_aa6);
var rad=Math.sqrt(Math.pow(_aaa[0],2)+Math.pow(_aaa[1],2));
var _aac=dojo.math.radToDeg(Math.atan(_aaa[1]/_aaa[0]));
if(_aaa[0]<0){
_aac-=90;
}else{
_aac+=90;
}
dojo.math.curves.CenteredArc.call(this,_aa9,rad,_aac,_aac+(ccw?-180:180));
},CenteredArc:function(_aad,_aae,_aaf,end){
this.center=_aad;
this.radius=_aae;
this.start=_aaf||0;
this.end=end;
this.getValue=function(n){
var _ab2=new Array(2);
var _ab3=dojo.math.degToRad(this.start+((this.end-this.start)*n));
_ab2[0]=this.center[0]+this.radius*Math.sin(_ab3);
_ab2[1]=this.center[1]-this.radius*Math.cos(_ab3);
return _ab2;
};
return this;
},Circle:function(_ab4,_ab5){
dojo.math.curves.CenteredArc.call(this,_ab4,_ab5,0,360);
return this;
},Path:function(){
var _ab6=[];
var _ab7=[];
var _ab8=[];
var _ab9=0;
this.add=function(_aba,_abb){
if(_abb<0){
dojo.raise("dojo.math.curves.Path.add: weight cannot be less than 0");
}
_ab6.push(_aba);
_ab7.push(_abb);
_ab9+=_abb;
computeRanges();
};
this.remove=function(_abc){
for(var i=0;i<_ab6.length;i++){
if(_ab6[i]==_abc){
_ab6.splice(i,1);
_ab9-=_ab7.splice(i,1)[0];
break;
}
}
computeRanges();
};
this.removeAll=function(){
_ab6=[];
_ab7=[];
_ab9=0;
};
this.getValue=function(n){
var _abf=false,_ac0=0;
for(var i=0;i<_ab8.length;i++){
var r=_ab8[i];
if(n>=r[0]&&n<r[1]){
var subN=(n-r[0])/r[2];
_ac0=_ab6[i].getValue(subN);
_abf=true;
break;
}
}
if(!_abf){
_ac0=_ab6[_ab6.length-1].getValue(1);
}
for(var j=0;j<i;j++){
_ac0=dojo.math.points.translate(_ac0,_ab6[j].getValue(1));
}
return _ac0;
};
function computeRanges(){
var _ac5=0;
for(var i=0;i<_ab7.length;i++){
var end=_ac5+_ab7[i]/_ab9;
var len=end-_ac5;
_ab8[i]=[_ac5,end,len];
_ac5=end;
}
}
return this;
}};
dojo.provide("dojo.math.points");
dojo.math.points={translate:function(a,b){
if(a.length!=b.length){
dojo.raise("dojo.math.translate: points not same size (a:["+a+"], b:["+b+"])");
}
var c=new Array(a.length);
for(var i=0;i<a.length;i++){
c[i]=a[i]+b[i];
}
return c;
},midpoint:function(a,b){
if(a.length!=b.length){
dojo.raise("dojo.math.midpoint: points not same size (a:["+a+"], b:["+b+"])");
}
var c=new Array(a.length);
for(var i=0;i<a.length;i++){
c[i]=(a[i]+b[i])/2;
}
return c;
},invert:function(a){
var b=new Array(a.length);
for(var i=0;i<a.length;i++){
b[i]=-a[i];
}
return b;
},distance:function(a,b){
return Math.sqrt(Math.pow(b[0]-a[0],2)+Math.pow(b[1]-a[1],2));
}};
dojo.provide("dojo.math.*");
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
var _ad6=0;
var _ad7=0;
for(var _ad8 in this._state){
_ad6++;
var _ad9=this._state[_ad8];
if(_ad9.isDone){
_ad7++;
delete this._state[_ad8];
}else{
if(!_ad9.isFinishing){
var _ada=_ad9.kwArgs;
try{
if(_ad9.checkString&&eval("typeof("+_ad9.checkString+") != 'undefined'")){
_ad9.isFinishing=true;
this._finish(_ad9,"load");
_ad7++;
delete this._state[_ad8];
}else{
if(_ada.timeoutSeconds&&_ada.timeout){
if(_ad9.startTime+(_ada.timeoutSeconds*1000)<(new Date()).getTime()){
_ad9.isFinishing=true;
this._finish(_ad9,"timeout");
_ad7++;
delete this._state[_ad8];
}
}else{
if(!_ada.timeoutSeconds){
_ad7++;
}
}
}
}
catch(e){
_ad9.isFinishing=true;
this._finish(_ad9,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_ad7>=_ad6){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_adb){
return dojo.lang.inArray(["text/javascript","text/json","application/json"],(_adb["mimetype"].toLowerCase()))&&(_adb["method"].toLowerCase()=="get")&&!(_adb["formNode"]&&dojo.io.formHasFile(_adb["formNode"]))&&(!_adb["sync"]||_adb["sync"]==false)&&!_adb["file"]&&!_adb["multipart"];
};
this.removeScripts=function(){
var _adc=document.getElementsByTagName("script");
for(var i=0;_adc&&i<_adc.length;i++){
var _ade=_adc[i];
if(_ade.className=="ScriptSrcTransport"){
var _adf=_ade.parentNode;
_adf.removeChild(_ade);
i--;
}
}
};
this.bind=function(_ae0){
var url=_ae0.url;
var _ae2="";
if(_ae0["formNode"]){
var ta=_ae0.formNode.getAttribute("action");
if((ta)&&(!_ae0["url"])){
url=ta;
}
var tp=_ae0.formNode.getAttribute("method");
if((tp)&&(!_ae0["method"])){
_ae0.method=tp;
}
_ae2+=dojo.io.encodeForm(_ae0.formNode,_ae0.encoding,_ae0["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _ae5=url.split("?");
if(_ae5&&_ae5.length==2){
url=_ae5[0];
_ae2+=(_ae2?"&":"")+_ae5[1];
}
if(_ae0["backButton"]||_ae0["back"]||_ae0["changeUrl"]){
dojo.undo.browser.addToHistory(_ae0);
}
var id=_ae0["apiId"]?_ae0["apiId"]:"id"+this._counter++;
var _ae7=_ae0["content"];
var _ae8=_ae0.jsonParamName;
if(_ae0.sendTransport||_ae8){
if(!_ae7){
_ae7={};
}
if(_ae0.sendTransport){
_ae7["dojo.transport"]="scriptsrc";
}
if(_ae8){
_ae7[_ae8]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_ae0.postContent){
_ae2=_ae0.postContent;
}else{
if(_ae7){
_ae2+=((_ae2)?"&":"")+dojo.io.argsFromMap(_ae7,_ae0.encoding,_ae8);
}
}
if(_ae0["apiId"]){
_ae0["useRequestId"]=true;
}
var _ae9={"id":id,"idParam":"_dsrid="+id,"url":url,"query":_ae2,"kwArgs":_ae0,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(_ae9,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_ae7&&_ae7[_ae8]){
_ae9.jsonp=_ae7[_ae8];
_ae9.jsonpCall=function(data){
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
if(_ae0["useRequestId"]||_ae0["checkString"]||_ae9["jsonp"]){
this._state[id]=_ae9;
}
if(_ae0["checkString"]){
_ae9.checkString=_ae0["checkString"];
}
_ae9.constantParams=(_ae0["constantParams"]==null?"":_ae0["constantParams"]);
if(_ae0["preventCache"]||(this.preventCache==true&&_ae0["preventCache"]!=false)){
_ae9.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
_ae9.nocacheParam="";
}
var _aeb=_ae9.url.length+_ae9.query.length+_ae9.constantParams.length+_ae9.nocacheParam.length+this._extraPaddingLength;
if(_ae0["useRequestId"]){
_aeb+=_ae9.idParam.length;
}
if(!_ae0["checkString"]&&_ae0["useRequestId"]&&!_ae9["jsonp"]&&!_ae0["forceSingleRequest"]&&_aeb>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(_ae9,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(_ae9,1);
}
}else{
var _aec=[_ae9.constantParams,_ae9.nocacheParam,_ae9.query];
if(_ae0["useRequestId"]&&!_ae9["jsonp"]){
_aec.unshift(_ae9.idParam);
}
var _aed=this._buildUrl(_ae9.url,_aec);
_ae9.finalUrl=_aed;
this._attach(_ae9.id,_aed);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_aef){
var _af0=url;
var _af1="?";
for(var i=0;i<_aef.length;i++){
if(_aef[i]){
_af0+=_af1+_aef[i];
_af1="&";
}
}
return _af0;
};
this._attach=function(id,url){
var _af5=document.createElement("script");
_af5.type="text/javascript";
_af5.src=url;
_af5.id=id;
_af5.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_af5);
};
this._multiAttach=function(_af6,part){
if(_af6.query==null){
this._finish(_af6,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!_af6.constantParams){
_af6.constantParams="";
}
var _af8=this.maxUrlLength-_af6.idParam.length-_af6.constantParams.length-_af6.url.length-_af6.nocacheParam.length-this._extraPaddingLength;
var _af9=_af6.query.length<_af8;
var _afa;
if(_af9){
_afa=_af6.query;
_af6.query=null;
}else{
var _afb=_af6.query.lastIndexOf("&",_af8-1);
var _afc=_af6.query.lastIndexOf("=",_af8-1);
if(_afb>_afc||_afc==_af8-1){
_afa=_af6.query.substring(0,_afb);
_af6.query=_af6.query.substring(_afb+1,_af6.query.length);
}else{
_afa=_af6.query.substring(0,_af8);
var _afd=_afa.substring((_afb==-1?0:_afb+1),_afc);
_af6.query=_afd+"="+_af6.query.substring(_af8,_af6.query.length);
}
}
var _afe=[_afa,_af6.idParam,_af6.constantParams,_af6.nocacheParam];
if(!_af9){
_afe.push("_part="+part);
}
var url=this._buildUrl(_af6.url,_afe);
this._attach(_af6.id+"_"+part,url);
};
this._finish=function(_b00,_b01,_b02){
if(_b01!="partOk"&&!_b00.kwArgs[_b01]&&!_b00.kwArgs["handle"]){
if(_b01=="error"){
_b00.isDone=true;
throw _b02;
}
}else{
switch(_b01){
case "load":
var _b03=_b02?_b02.response:null;
if(!_b03){
_b03=_b02;
}
_b00.kwArgs[(typeof _b00.kwArgs.load=="function")?"load":"handle"]("load",_b03,_b02,_b00.kwArgs);
_b00.isDone=true;
break;
case "partOk":
var part=parseInt(_b02.response.part,10)+1;
if(_b02.response.constantParams){
_b00.constantParams=_b02.response.constantParams;
}
this._multiAttach(_b00,part);
_b00.isDone=false;
break;
case "error":
_b00.kwArgs[(typeof _b00.kwArgs.error=="function")?"error":"handle"]("error",_b02.response,_b02,_b00.kwArgs);
_b00.isDone=true;
break;
default:
_b00.kwArgs[(typeof _b00.kwArgs[_b01]=="function")?_b01:"handle"](_b01,_b02,_b02,_b00.kwArgs);
_b00.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
if(typeof window!="undefined"){
window.onscriptload=function(_b05){
var _b06=null;
var _b07=dojo.io.ScriptSrcTransport;
if(_b07._state[_b05.id]){
_b06=_b07._state[_b05.id];
}else{
var _b08;
for(var _b09 in _b07._state){
_b08=_b07._state[_b09];
if(_b08.finalUrl&&_b08.finalUrl==_b05.id){
_b06=_b08;
break;
}
}
if(_b06==null){
var _b0a=document.getElementsByTagName("script");
for(var i=0;_b0a&&i<_b0a.length;i++){
var _b0c=_b0a[i];
if(_b0c.getAttribute("class")=="ScriptSrcTransport"&&_b0c.src==_b05.id){
_b06=_b07._state[_b0c.id];
break;
}
}
}
if(_b06==null){
throw "No matching state for onscriptload event.id: "+_b05.id;
}
}
var _b0d="error";
switch(_b05.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_b0d="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_b0d="load";
break;
}
_b07._finish(_b06,_b0d,_b05);
};
}
dojo.provide("dojo.io.cometd");
cometd=new function(){
this.initialized=false;
this.connected=false;
this.connectionTypes=new dojo.AdapterRegistry(true);
this.version=0.1;
this.minimumVersion=0.1;
this.clientId=null;
this._isXD=false;
this.handshakeReturn=null;
this.currentTransport=null;
this.url=null;
this.lastMessage=null;
this.globalTopicChannels={};
this.backlog=[];
this.tunnelInit=function(_b0e,_b0f){
};
this.tunnelCollapse=function(){
dojo.debug("tunnel collapsed!");
};
this.init=function(_b10,root,_b12){
_b10=_b10||{};
_b10.version=this.version;
_b10.minimumVersion=this.minimumVersion;
_b10.channel="/meta/handshake";
this.url=root||djConfig["cometdRoot"];
if(!this.url){
dojo.debug("no cometd root specified in djConfig and no root passed");
return;
}
var _b13={url:this.url,method:"POST",mimetype:"text/json",load:dojo.lang.hitch(this,"finishInit"),content:{"message":dojo.json.serialize([_b10])}};
var _b14="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=(""+window.location).match(new RegExp(_b14));
if(r[4]){
var tmp=r[4].split(":");
var _b17=tmp[0];
var _b18=tmp[1]||"80";
r=this.url.match(new RegExp(_b14));
if(r[4]){
tmp=r[4].split(":");
var _b19=tmp[0];
var _b1a=tmp[1]||"80";
if((_b19!=_b17)||(_b1a!=_b18)){
dojo.debug(_b17,_b19);
dojo.debug(_b18,_b1a);
this._isXD=true;
_b13.transport="ScriptSrcTransport";
_b13.jsonParamName="jsonp";
_b13.method="GET";
}
}
}
if(_b12){
dojo.lang.mixin(_b13,_b12);
}
return dojo.io.bind(_b13);
};
this.finishInit=function(type,data,evt,_b1e){
data=data[0];
this.handshakeReturn=data;
if(data["authSuccessful"]==false){
dojo.debug("cometd authentication failed");
return;
}
if(data.version<this.minimumVersion){
dojo.debug("cometd protocol version mismatch. We wanted",this.minimumVersion,"but got",data.version);
return;
}
this.currentTransport=this.connectionTypes.match(data.supportedConnectionTypes,data.version,this._isXD);
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
this._getRandStr=function(){
return Math.random().toString().substring(2,10);
};
this.deliver=function(_b21){
dojo.lang.forEach(_b21,this._deliver,this);
};
this._deliver=function(_b22){
if(!_b22["channel"]){
dojo.debug("cometd error: no channel for message!");
return;
}
if(!this.currentTransport){
this.backlog.push(["deliver",_b22]);
return;
}
this.lastMessage=_b22;
if((_b22.channel.length>5)&&(_b22.channel.substr(0,5)=="/meta")){
switch(_b22.channel){
case "/meta/subscribe":
if(!_b22.successful){
dojo.debug("cometd subscription error for channel",_b22.channel,":",_b22.error);
return;
}
this.subscribed(_b22.subscription,_b22);
break;
case "/meta/unsubscribe":
if(!_b22.successful){
dojo.debug("cometd unsubscription error for channel",_b22.channel,":",_b22.error);
return;
}
this.unsubscribed(_b22.subscription,_b22);
break;
}
}
this.currentTransport.deliver(_b22);
var _b23=(this.globalTopicChannels[_b22.channel])?_b22.channel:"/cometd"+_b22.channel;
dojo.event.topic.publish(_b23,_b22);
};
this.disconnect=function(){
if(!this.currentTransport){
dojo.debug("no current transport to disconnect from");
return;
}
this.currentTransport.disconnect();
};
this.publish=function(_b24,data,_b26){
if(!this.currentTransport){
this.backlog.push(["publish",_b24,data,_b26]);
return;
}
var _b27={data:data,channel:_b24};
if(_b26){
dojo.lang.mixin(_b27,_b26);
}
return this.currentTransport.sendMessage(_b27);
};
this.subscribe=function(_b28,_b29,_b2a,_b2b){
if(!this.currentTransport){
this.backlog.push(["subscribe",_b28,_b29,_b2a,_b2b]);
return;
}
if(_b2a){
var _b2c=(_b29)?_b28:"/cometd"+_b28;
if(_b29){
this.globalTopicChannels[_b28]=true;
}
dojo.event.topic.subscribe(_b2c,_b2a,_b2b);
}
return this.currentTransport.sendMessage({channel:"/meta/subscribe",subscription:_b28});
};
this.subscribed=function(_b2d,_b2e){
dojo.debug(_b2d);
dojo.debugShallow(_b2e);
};
this.unsubscribe=function(_b2f,_b30,_b31,_b32){
if(!this.currentTransport){
this.backlog.push(["unsubscribe",_b2f,_b30,_b31,_b32]);
return;
}
if(_b31){
var _b33=(_b30)?_b2f:"/cometd"+_b2f;
dojo.event.topic.unsubscribe(_b33,_b31,_b32);
}
return this.currentTransport.sendMessage({channel:"/meta/unsubscribe",subscription:_b2f});
};
this.unsubscribed=function(_b34,_b35){
dojo.debug(_b34);
dojo.debugShallow(_b35);
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
this.check=function(_b36,_b37,_b38){
return ((!_b38)&&(!dojo.render.html.safari)&&(dojo.lang.inArray(_b36,"iframe")));
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
this.deliver=function(_b39){
if(_b39["timestamp"]){
this.lastTimestamp=_b39.timestamp;
}
if(_b39["id"]){
this.lastId=_b39.id;
}
if((_b39.channel.length>5)&&(_b39.channel.substr(0,5)=="/meta")){
switch(_b39.channel){
case "/meta/connect":
if(!_b39.successful){
dojo.debug("cometd connection error:",_b39.error);
return;
}
this.connectionId=_b39.connectionId;
this.connected=true;
this.processBacklog();
break;
case "/meta/reconnect":
if(!_b39.successful){
dojo.debug("cometd reconnection error:",_b39.error);
return;
}
this.connected=true;
break;
case "/meta/subscribe":
if(!_b39.successful){
dojo.debug("cometd subscription error for channel",_b39.channel,":",_b39.error);
return;
}
dojo.debug(_b39.channel);
break;
}
}
};
this.widenDomain=function(_b3a){
var cd=_b3a||document.domain;
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
this.postToIframe=function(_b3d,url){
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
for(var x in _b3d){
var tn;
if(dojo.render.html.ie){
tn=document.createElement("<input type='hidden' name='"+x+"' value='"+_b3d[x]+"'>");
this.phonyForm.appendChild(tn);
}else{
tn=document.createElement("input");
this.phonyForm.appendChild(tn);
tn.type="hidden";
tn.name=x;
tn.value=_b3d[x];
}
}
this.phonyForm.submit();
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_b41,_b42){
if((_b42)||(this.connected)){
_b41.connectionId=this.connectionId;
_b41.clientId=cometd.clientId;
var _b43={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_b41])}};
return dojo.io.bind(_b43);
}else{
this.backlog.push(_b41);
}
};
this.startup=function(_b44){
dojo.debug("startup!");
dojo.debug(dojo.json.serialize(_b44));
if(this.connected){
return;
}
this.rcvNodeName="cometdRcv_"+cometd._getRandStr();
var _b45=cometd.url+"/?tunnelInit=iframe";
if(false&&dojo.render.html.ie){
this.rcvNode=new ActiveXObject("htmlfile");
this.rcvNode.open();
this.rcvNode.write("<html>");
this.rcvNode.write("<script>document.domain = '"+document.domain+"'");
this.rcvNode.write("</html>");
this.rcvNode.close();
var _b46=this.rcvNode.createElement("div");
this.rcvNode.appendChild(_b46);
this.rcvNode.parentWindow.dojo=dojo;
_b46.innerHTML="<iframe src='"+_b45+"'></iframe>";
}else{
this.rcvNode=dojo.io.createIFrame(this.rcvNodeName,"",_b45);
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
this.check=function(_b47,_b48,_b49){
return ((!_b49)&&(dojo.render.html.mozilla)&&(dojo.lang.inArray(_b47,"mime-message-block")));
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
this.openTunnelWith=function(_b4b,url){
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
dojo.debug(dojo.json.serialize(_b4b));
this.xhr.send(dojo.io.argsFromMap(_b4b,"utf8"));
};
this.processBacklog=function(){
while(this.backlog.length>0){
this.sendMessage(this.backlog.shift(),true);
}
};
this.sendMessage=function(_b4d,_b4e){
if((_b4e)||(this.connected)){
_b4d.connectionId=this.connectionId;
_b4d.clientId=cometd.clientId;
var _b4f={url:cometd.url||djConfig["cometdRoot"],method:"POST",mimetype:"text/json",content:{message:dojo.json.serialize([_b4d])}};
return dojo.io.bind(_b4f);
}else{
this.backlog.push(_b4d);
}
};
this.startup=function(_b50){
dojo.debugShallow(_b50);
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
this.check=function(_b51,_b52,_b53){
return ((!_b53)&&(dojo.lang.inArray(_b51,"long-polling")));
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
this.openTunnelWith=function(_b54,url){
dojo.io.bind({url:(url||cometd.url),method:"post",content:_b54,mimetype:"text/json",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_b5a,_b5b){
if((_b5b)||(this.connected)){
_b5a.connectionId=this.connectionId;
_b5a.clientId=cometd.clientId;
var _b5c={url:cometd.url||djConfig["cometdRoot"],method:"post",mimetype:"text/json",content:{message:dojo.json.serialize([_b5a])},load:dojo.lang.hitch(this,function(type,data,evt,args){
cometd.deliver(data);
})};
return dojo.io.bind(_b5c);
}else{
this.backlog.push(_b5a);
}
};
this.startup=function(_b61){
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
this.check=function(_b62,_b63,_b64){
return dojo.lang.inArray(_b62,"callback-polling");
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
this.openTunnelWith=function(_b65,url){
var req=dojo.io.bind({url:(url||cometd.url),content:_b65,mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",load:dojo.lang.hitch(this,function(type,data,evt,args){
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
this.sendMessage=function(_b6c,_b6d){
if((_b6d)||(this.connected)){
_b6c.connectionId=this.connectionId;
_b6c.clientId=cometd.clientId;
var _b6e={url:cometd.url||djConfig["cometdRoot"],mimetype:"text/json",transport:"ScriptSrcTransport",jsonParamName:"jsonp",content:{message:dojo.json.serialize([_b6c])}};
return dojo.io.bind(_b6e);
}else{
this.backlog.push(_b6c);
}
};
this.startup=function(_b6f){
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

