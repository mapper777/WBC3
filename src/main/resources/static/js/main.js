onload = function() {
  /*
  let ua = navigator.userAgent;
   if (ua.search(/Edge/)>-1) uaCode=1;
   if (ua.search(/MSIE/)>-1) uaCode=2;
   if (ua.search(/Trident/)>-1) uaCode=3;
   if (ua.search(/Firefox/)>-1) {uaCode=4;deltaWidth=-6;deltaHeight=-6;}
   if (ua.search(/Opera/)>-1) uaCode=5;
   if (ua.search(/OPR/)>-1) uaCode=6;
   if (ua.search(/YaBrowser/)>-1) {uaCode=7;deltaWidth=-6;deltaHeight=-6;}
   if (ua.search(/Chrome/)>-1) uaCode=8;
   if (ua.search(/Safari/)>-1) uaCode=9;
   if (ua.search(/Maxthon/)>-1) uaCode=10;
  */
//if((_G==undefined)||(_G==null))
  _G.prefix=window._INIT.prefix;
  _G.wUser=window._INIT.wUser;
  _G.job=window._INIT.jobs;
  _G.divMnu = document.getElementById("mnu");
  _G.divExt = document.getElementById("ext");
  _G.divRules = document.getElementById("rules");
  _G.divAny = document.getElementById("any");
  _G.divRes = document.getElementById("resB");
  _G.tabR = document.getElementById("rls");
  _G.openR = document.getElementById("opR");
  _G.openT = document.getElementById("opRules");
  _G.saveR = document.getElementById("rSave");
  _G.opnM = document.getElementById("opn");
  _G.txtCod = document.getElementById("codT");
  _G.divTun = document.getElementById("tunE");
  _G.vIew=document.getElementById("vieW");
  _G.builD=document.getElementById("bUild");
  _G.ruN=document.getElementById("rUn");
  _G.tsT=document.getElementById("tSt");
  _G.tunE=document.getElementById("tUne");
  _G.rdeL=document.getElementById("rDel");
  _G.rclR=document.getElementById("rClr");
  _G.rnamE=document.getElementById("rName");
  _G.extTH=document.getElementById("extT");
  _G.rnorM=document.getElementById("rNorm");
  _G.ifr=document.getElementById("iFr");
  _G.helP=window._INIT.helP;
  _G.rParser=new ruleParser();
  _G.inpBox = document.createElement("input");
  _G.inpBox.classList.add("abs");
  document.body.appendChild(_G.inpBox);

  _G.divAny.style.visibility="hidden";
  _G.divRes.style.visibility="hidden";
  _G.inpBox.style.visibility="hidden";
  _G.tabR.rows[1].onclick = ruleClick;
  _G.opnM.onclick = shOpenR;
  _G.vIew.onclick=shViewT;
  _G.saveR.onclick = saveRules;
  _G.builD.onclick=function(){viewSwitch(1);buildT(0);};
  _G.ruN.onclick=runT;
  _G.tsT.onclick=tesT;
  _G.tunE.onclick=viewTune;
  _G.opnM.style.cursor = "pointer";
  _G.saveR.style.cursor = "pointer";
  _G.rclR.style.cursor = "pointer";
  _G.rdeL.style.cursor = "pointer";
  _G.builD.style.cursor = "pointer";
  _G.ruN.style.cursor = "pointer";
  _G.tsT.style.cursor = "pointer";
  _G.tunE.style.cursor = "pointer";
  _G.vIew.style.cursor = "pointer";
  _G.tabR.style.cursor="pointer";
  _G.builD.classList.add("grSp");
  _G.rdeL.classList.add("grSp");
  document.getElementById("dwnL").style.cursor = "pointer";
  document.getElementById("hElp").style.cursor = "pointer";
  document.getElementById("goOut").style.cursor = "pointer";
  let tr, oT=_G.openT;
  for(tr in oT.rows){
    oT.rows[tr].onclick = openRules;
  }
  document.getElementById("vCase").addEventListener("click",viewAny);
  _G.rnamE.addEventListener("keyup",function(e){if(e.keyCode===13)saveRules();});
  _G.rdeL.addEventListener("click",rulesDel);
  _G.rclR.addEventListener("click",rClear);
  document.getElementById("goOut").addEventListener("click",stop);
  document.getElementById("cMnu").addEventListener("click",contMenu);
  document.getElementById("hElp").addEventListener("click",helpOpen);
  _G.timerI=setInterval(timer, 60000);

  _G.divTun.style.display="none";
  setInterval(_G.test, 500);
  _G.dTime=window._INIT.dTime;
  _G.tplName=window._INIT.tplName;
  _G.autoS=window._INIT.autoS;
  _G.saveNN=window._INIT.saveNN;
  window.onresize=onresize;
  onresize();
  viewSwitch(1);
  var ll=document.getElementsByName("lang");
  for(var i=0;i<ll.length;i++){
    if(ll[i].checked){
      langChng(ll[i]);
      if(ll[i].getAttribute("value")!=="js"){
        _G.ruN.classList.add("grSp");
        break;
      }
    }
  }
  var ll=document.getElementsByName("lex");
  for(var i=0;i<ll.length;i++){
    if(ll[i].checked){
      tplChng(ll[i]);
    }
  }
  document.getElementById("flame1").style.backgroundColor="#00ff00";
  document.getElementById("flame2").style.backgroundColor="#eeeeee";
  document.getElementById("flame3").style.backgroundColor="#eeeeee";

  if (window._INIT.rulesList.length===0) {
    _G.mongo=false;
    _G.dTime=5000;
    showResp("База данных недоступна, невозможно открывать/сохранять системы правил и настройки, остальной функционал может использоваться");
  }

  viewSwitch(3);
  viewT(true);
}




onresize=function(){
  //.scrollTop / .scrollLeft + .getBoundingClientRect().top / .getBoundingClientRect().left
  let docElem=document.documentElement;
  let docH = (window.clientHeight || docElem.clientHeight || document.body.clientHeight);
//  _G.extTH.value=docH;
  _G.divMnu.style.height = docH - _G.divMnu.offsetTop - 20 + "px";
  _G.divExt.style.left = _G.divMnu.offsetLeft + _G.divMnu.clientWidth + 2 + "px";
  _G.divExt.style.width = docElem.scrollWidth - _G.divExt.offsetLeft - 20 + "px";
  _G.divRules.style.left = _G.divMnu.offsetLeft + _G.divMnu.clientWidth + 2 + "px";
  _G.divRules.style.top = _G.divExt.offsetTop + _G.divExt.offsetHeight + "px";
  _G.divRules.style.width = docElem.scrollWidth - _G.divRules.offsetLeft - 20 + "px";
  _G.divRules.style.height = docH - _G.divRules.offsetTop - 20 + "px";
//  fenceOver()
};
contMenu=function(e){
  var ip=parseInt(_G.testing.getAttribute("itemprop"));
  switch (e.target.parentElement.parentElement.getAttribute("itemProp")){
    case "d":
      _G.tmpRule=_G.tabR.rows[ip].innerHTML;
      _G.tabR.deleteRow(ip);
      _G.tR.splice(ip-1,1);
      _G.analyze();
      break;
    case "a":
      var newRow=_G.tabR.insertRow(ip);
      newRow.onclick = ruleClick;
      newRow.addEventListener("contextmenu",mouseButUp);
      var newC=newRow.insertCell();
      newC.classList.add("rgt");
      newC.innerHTML="<span>&nbsp;</span>";
      newC=newRow.insertCell();
      newC.innerHTML="<span>&nbsp;</span>";
      _G.tR.splice(ip-1,0,"");
      ruleClick(null,newRow);
      break;
    case "c":
      _G.tmpRule=_G.tabR.rows[ip].innerHTML;
      break;
    case "i":
      var newRow=_G.tabR.insertRow(ip);
      var i=_G.tmpRule.indexOf(":");
      if(i<0)
        newRow.innerHTML=_G.tmpRule;
      else
        newRow.innerHTML=_G.tmpRule.substring(0,i)+_G.tmpRule.substring(i+1);
      newRow.addEventListener("contextmenu",mouseButUp);
      newRow.onclick = ruleClick;
      _G.tR.splice(ip-1,0,newRow.innerText);
      _G.analyze();
  }
  _G.testing.style.display="none";
}
ruleClick = function(ev,row){
//  event = event || window.event;
  if(row===undefined)
    row=this;
  let coord = getOffset(row);
  let inpB=_G.inpBox;
  inpB.style.visibility = "visible";
  inpB.style.top = coord.top + "px";
  inpB.style.left = coord.left +"px";
  inpB.style.height = row.offsetHeight + _G.deltaHeight +"px";
  inpB.style.width = row.clientWidth + _G.deltaWidth + "px";
  let ri=row.rowIndex;
  if((ri>0)&&(ri<_G.tabR.rows.length-1))
    inpB.value=_G.tR[ri-1];
  else
    inpB.value="";
  inpB.oldValue=inpB.value;
  inpB.row = row;
  inpB.onblur = inpBoxBlur;
  inpB.onkeyup = function(e){_G.inpBox.lastKey=e.keyCode;if(e.keyCode===13) inpBoxBlur(e); else if(e.keyCode===27) _G.inpBox.style.visibility = "hidden";};
  inpB.focus();
};
mouseButUp = function(event){
  if(event.button===2){
    event.preventDefault();
    _G.testing=document.getElementById("cntxt");
    let row=event.target;
    while((row!==null) && (row.tagName!=="TR"))
      row=row.parentElement;
    let rowI=row.rowIndex;
    _G.testing.setAttribute("itemprop",rowI);
    showOpenR(event.target);
  }
}
function getOffset(elem) {
let docElem = document.documentElement;
let box;
    try {
        box = elem.getBoundingClientRect();
    } catch(e) {
	return { top: 0, left: 0 };
    }
  let body = document.body,
    clientTop = docElem.clientTop || body.clientTop || 0,
    clientLeft = docElem.clientLeft || body.clientLeft || 0,
    scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop,
    scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft,
    top = box.top + scrollTop - clientTop,
    left = box.left + scrollLeft - clientLeft;
    return { top: top, left: left };
}
inpBoxBlur = function(e) {
  let inpB=_G.inpBox;
//  if(inpB.visibility!=="hidden"){
  if(inpB.lastKey!==27){
    let t=inpB.row.parentElement.parentElement;
    let r=inpB.value.trim();
    inpB.style.top = "0px";
    inpB.style.visibility = "hidden";
    if((inpB.row.rowIndex===t.rows.length-1)&&(r.length>0)){
      let newRow=t.insertRow();
      newRow.onclick = ruleClick;
      newRow.addEventListener("contextmenu",mouseButUp);
      let newC=newRow.insertCell();
      newC.classList.add("rgt");
      newC.innerHTML="<span>&nbsp;</span>";
      newC=newRow.insertCell();
      newC.innerHTML="<span>&nbsp;</span>";
    }
    if(inpB.value!==inpB.oldValue)
      _G.newRule(inpB.row.rowIndex,r);
  }
}
function rClear(){
  viewSwitch(1);
  _G.clear();
  _G.rnamE.value="";
  while(_G.tabR.rows.length>2)
    _G.tabR.deleteRow(1);
  let rNorm=_G.rnorM;
  while(rNorm.rows.length>1)
    rNorm.deleteRow(1);
  _G.builD.classList.add("grSp");
  _G.rdeL.classList.add("grSp");
  _G.saveR.classList.add("grSp");
  _G.extTH.value="";
}
function shViewT(event){
  if(_G.vIew.classList.toString().indexOf("grSp")>=0)
    return;
  viewSwitch(1);
  _G.testing = document.getElementById("forV");
  showOpenR(event.target);
}
function shOpenR(event){
  _G.testing = _G.openR;
  showOpenR(event.target);
}
function showOpenR(t) {
  viewSwitch(1);
  let os=getOffset(t);
  _G.testing.target=t;
  _G.testing.style.display = "block";
  _G.testing.style.left=os.left+10+"px";
  _G.testing.style.top=os.top+t.offsetHeight+"px";
  document.body.addEventListener("mousemove", testClose);
}
function stop(e){
  if(_G.rulesChng&&(_G.autoS===2))
    saveRules();
  //window.location.href = "/logout";
  openRules(e,_G.prefix+"/logout");
}
function viewAny(evt){
  _G.testing.style.display="none";
  let w=parseInt(evt.target.parentElement.getAttribute("what"));
  if(w===0){
    viewSwitch(3);
    viewT();
  }else if(w===9){
    viewSwitch(3);
    viewR();
  } else {
    viewSwitch(2);
    buildT(w);
  }
  timeClear();
}
function viewR(){
  let i,sp="",nt,fl=true,k;
  let rNorm=_G.rnorM;
  for(i=rNorm.rows.length-1;i>0;i-=1){
    nt=rNorm.rows[i].cells[0].innerHTML.trim();
    if(nt!==""){
      k=_G.gS.indexOf(nt);
      if(fl&&(k>0)&&(_G.gP[k]!=4)){
        fl=false;
        sp="\n/*Лексические правила:*/\n"+sp;
      }
      sp=nt+":\t"+rNorm.rows[i].cells[1].innerHTML+"\n"+sp;
    }
  }
  sp="\t/*Система реальных правил:*/\n/*Расширение:*/\n"+_G.extTH.value+"\n/*Синтаксические правила:*/\n"+sp;
  rNorm=_G.tabR;
  let xt="";
  nt="";
  for(i=1;i<rNorm.rows.length-1;i+=1){
    nt=rNorm.rows[i].cells[0].children[0].innerHTML.trim()+":\t";
    let cll=rNorm.rows[i].cells[1].children;
    for(k=0;k<cll.length;k++)
      nt+=" "+cll[k].innerHTML;
    xt+="\n"+nt;
  }
  sp+="\n\n\t/*Система видимых/редактируемых правил:*/"+xt;
  _G.txtCod.value=sp.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&amp;","&");
}
function viewSwitch(w){
  switch (w) {
    case 1:
      _G.divRes.style.visibility="hidden";
      _G.divExt.style.visibility="visible";
      _G.divRules.style.visibility="visible";
      _G.divAny.style.visibility="hidden";
      _G.txtCod.style.display="none";
      _G.divTun.style.display="none";
      _G.ifr.style.display="none";
      break;
    case 2:
      _G.divAny.style.top=_G.divExt.offsetTop+"px";
      _G.divAny.style.left=_G.divExt.offsetLeft-2+"px";
      _G.divAny.style.width=_G.divExt.offsetWidth-2+"px";
      _G.divAny.style.height=_G.divMnu.offsetHeight-5+"px";
      _G.divExt.style.visibility="hidden";
      _G.divRules.style.visibility="hidden";
      _G.divAny.style.visibility="visible";
      _G.txtCod.style.display="none";
      _G.divTun.style.display="none";
      _G.ifr.style.display="none";
      break;
    case 3:
      _G.txtCod.style.display="block";
      _G.txtCod.style.top=_G.divExt.offsetTop+1+"px";
      _G.txtCod.style.left=_G.divExt.offsetLeft+1+"px";
      _G.txtCod.style.width=_G.divExt.offsetWidth-2+"px";
      _G.txtCod.style.height=_G.divMnu.offsetHeight-8+"px";
      _G.divRes.style.visibility="hidden";
      _G.divExt.style.visibility="hidden";
      _G.divRules.style.visibility="hidden";
      _G.divTun.style.display="none";
      _G.ifr.style.display="none";
        break;
    case 4:
      _G.divTun.style.top=_G.divExt.offsetTop+"px";
      _G.divTun.style.left=_G.divExt.offsetLeft-2+"px";
      _G.divTun.style.width=_G.divExt.offsetWidth-2+"px";
      _G.divTun.style.height=_G.divMnu.offsetHeight-5+"px";
      _G.divExt.style.visibility="hidden";
      _G.divRules.style.visibility="hidden";
      _G.divAny.style.visibility="hidden";
      _G.divTun.style.display="block";
      _G.txtCod.style.display="none";
      _G.ifr.style.display="none";
      break;
    case 5:
      _G.ifr.style.top=_G.divExt.offsetTop+"px";
      _G.ifr.style.left=_G.divExt.offsetLeft-2+"px";
      _G.ifr.style.width=_G.divExt.offsetWidth-2+"px";
      _G.ifr.style.height=_G.divMnu.offsetHeight-5+"px";
      _G.divExt.style.visibility="hidden";
      _G.divRules.style.visibility="hidden";
      _G.divAny.style.visibility="hidden";
      _G.divTun.style.display="none";
      _G.ifr.style.display="block";
      _G.txtCod.style.display="none";
      break;
  }
}
function viewTune(){
  viewSwitch(4);
}
function scrText(){
  let parser;

  TextReader.setText(inpTxt.value);
  _G.inpT=inpTxt.value;
  
  try {
      tracer.clear();
  } catch (e) {;}
  if(!(typeof parser == 'object'))
      parser = new Parser(TextReader);
  let b = document.getElementById("stepsB").value;
  b = +b;
  let e = document.getElementById("stepsE").value;
  e = +e;
  while((b + e) > 200){
      if(b > 10)
          b -= 10;
      if(e > 10)
          e -= 10;
  }
  document.getElementById("pRez").innerText = parser.parse(b, e);
  document.getElementById("lCnt").innerText = parser.getInfo(0);
  document.getElementById("cCnt").innerText = parser.getInfo(1);
  if(document.getElementById("showW").checked){
      let innT = parser.getInfo(2);
      let rezT ="";
      for(let i=0;i<innT.length;i++)
       rezT+="["+innT[i][0]+","+innT[i][1]+"] ";
      document.getElementById("rez").innerText = "Прочитанные лексемы: " + rezT;     
  } else
      document.getElementById("rez").innerText = "";
  if(document.getElementById("showH").checked && ((b + e) > 0)){
      let t = "";
      let h = parser.getInfo(3);
      if(h.length > 0){
          for(let i = 0; i <h.length; i++){
              let item = h[i];
              t += "<tr>";
              for(let j = 0; j < item.length; j++)
                  t += "<td>&nbsp;" + item[j] + "&nbsp;";
          }
          h = null;
          document.getElementById("hist").innerHTML = t;
          document.getElementById("history").style.display = "block";
      }
  }else{
      document.getElementById("history").style.display = "none";
      document.getElementById("hist").innerHTML = "<tr><td>";
  }
  try{
      document.getElementById("trace").innerText = tracer.getAll();
      if(document.getElementById("trace").innerText.trim() !== "")
          document.getElementById("traceShow").style.display = "block";
      else
          document.getElementById("traceShow").style.display = "none";
  } catch(e) {;}
};
function tesT(){
  if(_G.tsT.classList.toString().indexOf("grSp")>=0)
    return;
  viewSwitch(2);
}
function runT(){
  if(_G.ruN.classList.toString().indexOf("grSp")>=0)
    return;
  viewSwitch(2);
  let e;
  for(let i=0;i<document.head.children.length;i++)
    if((document.head.children[i].toString()==="[object HTMLScriptElement]")&&((ind=document.head.children[i].src.indexOf("translator"))>0)){
      document.head.removeChild(document.head.children[i]);
    }
  var script = document.createElement('script');
  script.type = 'text/javascript';
  script.src="users/"+_G.wUser+"translator.txt?v="+(""+Math.random()).substring(2,6);
  document.head.appendChild(script);
  _G.divAny.innerHTML="<div align=center>Лексика: <span id='lexName'></span></div><div align=center>Синтаксис: <span id='syntName'></span></div>\
<div align=center><textarea id=inpTxt rows=10 cols=60></textarea></div><div align=center><input type=button id='testText' align=center value='Проанализировать текст'></div>\
<div align=center><input id='showW' type='checkbox' checked='checked'>Показывать лексемы ({токен,'слово'})\
<input id='showH' type='checkbox' onclick=\"if(this.checked){document.getElementById('stepsB').value=50;document.getElementById('stepsE').value=50}\">Показывать историю разбора, шаги {0 : \
<input id='stepsB' type='text' size='2'>} и {последний - <input id = 'stepsE' type='text' size = '2'> : последний}</div>\
<div align=center><table border='1' style='border-collapse:collapse;'>\
<tr><td align='right' style='width:20em'>Результат анализа/трансляции:&nbsp;<td id='pRez' style='width:6em'>\
<tr><td align='right'>Шагов лексического анализатора:&nbsp;<td id='lCnt'>\
<tr><td align='right'>Шагов синтаксического анализатора:&nbsp;<td id='cCnt'></table></div>\
<div style='font-size:x-small'>&nbsp;</div><div id='rez' overflow=auto></div><div id='history' style='display:none'><hr>История разбора:\
<table id='hist' border='1' style='border-collapse:collapse;'><tr><td>&nbsp;</table></div>\
<div id='traceShow' style='display:none'><hr>Результат выполнения действий:<div id='trace'></div>";
  e=document.getElementById("testText");
  e.addEventListener('click', scrText, false);  
  
  if(_G.inpT!==undefined)
    inpTxt.value=_G.inpT;
  setTimeout('try{var e=Parser;}catch{alert(e);}',1000);
}



function viewT(w){
//  viewSwitch(2);
  let xmlhttp;
  if (window.XMLHttpRequest) {
    xmlhttp=new XMLHttpRequest();
  } else {
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(w){
    if (xmlhttp.readyState===4 && xmlhttp.status===200){
      let re;
      if(w)
        re = xmlhttp.responseText;
      else
        re = xmlhttp.responseText.trim();
/*      if((re.length===0)||"Error:"===re.substring(0,6)){
        alert("Failed to open rules system. "+re);
        return;
      }*/
      if("<"===re.substring(0,1)){
        stop();
        return;
      }
      _G.txtCod.value=re;
    }
  }
  if(w===undefined)
   //xmlhttp.open("GET",_G.prefix+"getFile.jsp?file="+_G.wUser+"translator.txt",true);
    xmlhttp.open("GET",_G.prefix+"/file?filename="+_G.wUser+"translator.txt",true);

  else
   //xmlhttp.open("GET",_G.prefix+"getFile.jsp?file=../help/history.txt",true)
      // вот тут запросик, который getFile и принимал!!!
    xmlhttp.open("GET",_G.prefix+"/file?filename=../static/help/history.txt",true);


  xmlhttp.responseType = 'text';
  xmlhttp.send();
  timeClear();
}

function openRules(e,url){
  viewSwitch(1);
  if((url!==_G.prefix+"/logout"/*"goOut.jsp"*/)&&(!_G.mongo||(this.className === "coll")))
    return;
  let cll = "";
  if(url!==_G.prefix+"/logout"/*"goOut.jsp"*/){
    let tbl=this.parentElement.parentElement;
    _G.testing.style.display = "none";
    _G.testing = undefined;
  //  document.body.removeEventListener("mousemove", testClose);
    let i = this.rowIndex-1;
    for(;i>=0;i-=1)
      if(tbl.rows[i].className==="coll"){
        cll=tbl.rows[i].cells[0].innerText.trim();
        break;
      }
    if(cll.length===0){
      alert("Не удалось получить доступ к базе данных.");
      return;
    }
  }
  let xmlhttp;
  if (window.XMLHttpRequest) {
    xmlhttp=new XMLHttpRequest();
  } else {
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(){
    if (xmlhttp.readyState===4 && xmlhttp.status===200){
      let re = xmlhttp.responseText.trim();
      if((re.length===0)||"Error:"===re.substring(0,6)){
        alert("Не удалось открыть систему правил. "+re);
        return;
      }
      if("<"===re.substring(0,1)){
        if((re.indexOf("password")>0)||(url===_G.prefix+"/logout"/*"goOut.jsp"*/))
          document.location.href=_G.prefix+"/auth/login"/*"index.jsp"*/;
        else
         alert("Не удалось открыть систему правил. Попробуйте выйти и войти заново.");
//        stop();
        return;
      }
      docToTable(re);
      _G.rulesChng=false;
      _G.rdeL.classList.remove("grSp");
    }
  }
  if(url!==_G.prefix+"/logout"/*"goOut.jsp"*/){
    let nam=this.cells[0].innerText.trim();
    url = _G.prefix+"/rules/getRules?name="+cll.substring(0,cll.length-1)+"&coll=" + (nam===""?"noName":nam);
    //url = _G.prefix+"/rules?coll="+cll.substring(0,cll.length-1)+"&name=" + (nam===""?"noName":nam);
    // url = _G.prefix+"getRules.jsp?coll="+cll.substring(0,cll.length-1)+"&name=" + (nam===""?"noName":nam);
  }
  xmlhttp.open("GET",url,true);
  xmlhttp.responseType = 'text';
  xmlhttp.send();
  timeClear();
}
  
function docToTable(json) {
  let js=null;
  try {
    js = JSON.parse(json);
  } catch(e){
    alert("Не удалось прочитать JSON-представление правил. "+e.toString());
    return;
  }
  _G.clear();
  if(js.data.extension!==undefined)
    _G.extTH.value=js.data.extension;
  else
    _G.extTH.value="";
  let r = js.data.rList;
  let i = 0;
//  alert("Rules: "+r.length+" Rows: "+tabR.rows.length);
  for(; i < r.length; i++){
    _G.newJRule(i+1,r[i]);
  }
  _G.tabR.rows[1].addEventListener("contextmenu",mouseButUp);
  for(;i < _G.tabR.rows.length;){
    _G.tabR.deleteRow(i);
  }
  let sautos=_G.autoS;
  _G.autoS=0;
  _G.analyze();
  _G.autoS=sautos;
  let newRow = _G.tabR.insertRow();
  newRow.onclick = ruleClick;
  newRow.addEventListener("contextmenu",mouseButUp);
  newRow.innerHTML = "<td class=rgt><span>&nbsp;</span><td>";
  _G.rnamE.value=(js.rules==="noName"?"":js.rules);
}
function testClose(evt){
  if(_G.testing === undefined)
    return;
  let os=getOffset(_G.testing);
  if((evt.x < os.left-15) || (evt.x > os.left+_G.testing.offsetWidth+15) || (evt.y < os.top-20) || (evt.y > os.top+_G.testing.offsetHeight+15)){
    _G.testing.style.display = "none";
    _G.testing = undefined;
    document.body.addEventListener("mousemove", testClose);
  }
}

function parseRule(row, rs){
  if(typeof(rs) !== "string"){
    let srs=rs.lPart;
    for(let j = 0; j < rs.rPart.length; j++)
      srs += " "+rs.rPart[j];
    rs=srs;
  }
  if(rs==="") {
//    row.rule=[];
    row.cells[0].innerHTML="<span>&nbsp;</span>";
    row.cells[1].innerHTML="<span>&nbsp;</span>";
    return;
  }
}
function showRule(ir, pr, ok, rs) {
  let row,fl;
  if(ir<_G.tabR.rows.length)
    row=_G.tabR.rows[ir];
  else{
    row = _G.tabR.insertRow(ir);
    row.onclick = ruleClick;
    row.addEventListener("contextmenu",mouseButUp);
    row.innerHTML = "<td class=rgt><td>";
  }
  if(_G.gP[_G.gS.indexOf(pr[0][1])]===4){
    for(let k=1;k<pr.length;k++){
      if((pr[k][0]===7)&&(k<pr.length-1)){
        ok=false;
        break;
      }
    }
  }
  if(ok)
    row.classList.remove("errBGr");
  else{
    row.classList.add("errBGr");
    _G.builD.classList.add("grSp");
  }
  let cl="normSpan",clp="",i,typ;
  typ=pr[0][0];
  if(typ<2)
    cl="grSp";
  else if(typ===2)
    cl="startNTSpan";
  else if(typ===3)
    cl="boldSpan";
  else if(typ===4)
    cl="grBoldSpan";
  else
    cl="grSp";
  row.cells[0].innerHTML = "<span class="+cl+">" + pr[0][1] + "</span><span class='normSpan'>:&nbsp;</span>";
  cl="";
  let ht = "";
  fl=false;
  for(i = 1; i < pr.length; i++){
    typ = pr[i][0];
    switch(typ) {
      case -1:
      case 0:
      case 1:
        cl="grSp";
        break;
      case 2:
        cl="startNTSpan";
        break;
      case 3:
        cl="boldSpan";
        break;
      case 4:
        cl="grBoldSpan";
        break;
      case 5:
        cl="grSpan";
        break;
      case 6:
        if(fl){
          cl="grSpanNoMargin"
          fl=false;
        }else
          cl="grSpan";
        break;
      case 7:
        if(_G.mode)
          pr[i][1]="{ ... }";
        cl="brSpan";
        break;
      case 8:
        cl="redSpan";
        clp=cl;
        break;
      case 9:
        cl="noMargin";
        break;
      case 10:
        cl="magSpan";
        break;
      case 11:
        fl=true;
        break;
    }
    let pr1=pr[i][1];
    if(typ<12)
      ht += "<span typ=" + typ + ((cl.length===0)?"":" class=" + cl)+">" + pr1 + "</span>";
    if(!ok){
      let ind = rs.indexOf(pr1);
      if(ind>=0)
        rs=rs.substring(ind+pr1.length);
    }
  }
  row.cells[1].innerHTML=ht + (ok?"":"<span>"+rs+"</span>");
}
function showNRules(wR,gS,gP){
  let rNorm=_G.rnorM;
  let ii=0,i,row,ru;
  while(ii < wR.length){
    if(ii>=rNorm.rows.length-1){
      row=rNorm.insertRow();
      row.innerHTML = "<td class=rgt><td>";
    }else
      row=rNorm.rows[ii+1];
//    row.cells[0].innerText=gS[wR[ii][0]]+"#"+gP[wR[ii][0]];
    row.cells[0].innerText=gS[wR[ii][0]];//+"#"+gP[wR[ii][0]];
    ru="";
    for(i=1;i<wR[ii].length;i++)
        ru+=gS[wR[ii][i]]+/*"#"+gP[wR[ii][i]]+*/"  ";
//        ru+=gS[wR[ii][i]]+"#"+gP[wR[ii][i]]+"  ";
    row.cells[1].innerText=ru;
    ii += 1;
  }
  ii+=1;
  while(ii<rNorm.rows.length)
    rNorm.deleteRow(ii);
  if(rNorm.rows.length>2){
    if(_G.mongo)
      _G.saveR.classList.remove("grSp");
    _G.builD.classList.remove("grSp");
    _G.vIew.classList.remove("grSp");
  }
}
function getJsonRules(){
  let jsonR = {rList:[]};
  var rm=false;
  if(_G.mode){
    rm=true;
    actionsS_H();
  }
  for(let i=1;i<_G.tabR.rows.length-1;i++){
    let lSymb=_G.tabR.rows[i].cells[0].innerText.trim();
    if(lSymb.indexOf(":")===lSymb.length-1)
      lSymb=lSymb.substring(0,lSymb.length-1);
    let rp=_G.tabR.rows[i].cells[1].children;
    let rul={lPart:lSymb, rPart:[]};
    for(let c=0;c<rp.length;c++){
      rul.rPart.push(rp[c].innerText);
    }
    jsonR.rList.push(rul);
  }
  if(rm)
    actionsS_H();
  return jsonR;
}
function getJsonRulesWT(){
  let jsonR = {rList:[],extension:""};
  for(let i=1;i<_G.tabR.rows.length-1;i++){
    let lSymb=_G.tabR.rows[i].cells[0].innerText.trim();
    if(lSymb.indexOf(":")===lSymb.length-1)
      lSymb=lSymb.substring(0,lSymb.length-2);
    let rp=_G.tabR.rows[i].cells[1].children;
    let rul={lPart:lSymb, rPart:[]};
    for(let c=0;c<rp.length;c++){
      rul.rPart.push(rp[c].innerText);
      rul.rPart.push(rp[c].getAttribute("typ"));
    }
    jsonR.rList.push(rul);
  }
  return jsonR;
}
function showResp(t){
  _G.divRes.style.visibility="visible";
  _G.divRes.innerText=t;
  _G.divRes.style.top=_G.divMnu.offsetHeight-_G.divRes.offsetHeight+6+"px";
  _G.divRes.style.width=_G.divMnu.offsetWidth+_G.divRules.offsetWidth-8+"px";
  setTimeout('_G.divRes.style.visibility="hidden";',_G.dTime);  
}
function saveRules(){
  if(!_G.mongo)
    return;
  let rN=_G.rnamE;
  if(rN.value==='')
    rN.value="noName";
  if (!rN.value.match(/^[0-9a-zA-Zа-яА-Я]+$/)){
    alert("Имя системы правил может содержать только буквы или цифры.");
    return;
  }
  let jsonR = getJsonRules();
  let i,xmlhttp;
  if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp=new XMLHttpRequest();
  } else {// code for IE6, IE5
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(){
    if (xmlhttp.readyState===4 && (xmlhttp.status===200 || xmlhttp.status===201)){
      _G.rulesChng=false;
      let re = xmlhttp.responseText.trim();
      if(re.charAt(0)==="<"){
        stop();
        return;
      } else if("Error"===re.substring(0,5))
        alert("Не удалось сохранить систему правил: " + xmlhttp.responseText.substring(7));
      else{
        let rn=_G.rnamE.value.trim();
        if(_G.dTime>100)
          showResp(" Система правил '"+rn+"' сохранена");
        let oR=_G.openT;
        let fnd=false;
        for(let i=oR.rows.length-1;i>0;i-=1){
          if(oR.rows[i].cells[0].innerText.trim()===rn){
            fnd=true;
            break;
          }
        }
        if(!fnd){
          let row=oR.insertRow(oR.rows.length);
          row.onclick=openRules;
          let cell=row.insertCell(0);
          cell.innerHTML="&nbsp;<span>"+rn+"&nbsp;</span>";
        }
      }
//      ans=1;
    }
  }
  //let url=_G.prefix+"saveRules.jsp";
  let url=_G.prefix+"/rules/save";
  xmlhttp.open("POST",url,true);
  xmlhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
  let jD={rules:'',data:jsonR};
  jsonR.extension=_G.extTH.value;
  jD.rules=rN.value;
  let dataR=JSON.stringify(jD);
  xmlhttp.send(dataR);
  timeClear();
}
function rulesDel(){
  if(!_G.mongo||(_G.rdeL.classList.toString().indexOf("grSp")>=0))
    return;
  let rN=_G.rnamE.value.trim();
  if(rN==='')
    rN="noName";
  if(!confirm("Вы действительно хотите удалить систему правил '"+rN+"'?"))
    return;
  let xmlhttp;
  if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp=new XMLHttpRequest();
  } else {// code for IE6, IE5
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(){
    if (xmlhttp.readyState===4 && (xmlhttp.status===200 || xmlhttp.status===204)){
      let re = xmlhttp.responseText.trim();
      if(re.charAt(0)==="<"){
        stop();
        return;
      } else if("Error"===re.substring(0,5))
        alert("Не удалось удалить систему правил: " + xmlhttp.responseText.substring(7));
      else{
        let oR=_G.openT;
        let rn=_G.rnamE.value.trim();
        for(let i=oR.rows.length-1;i>0;i-=1){
          if(oR.rows[i].cells[0].innerText.trim()===rn){
            oR.deleteRow(i);
            break;
          }
        }
        rClear();
        if(_G.dTime>100)
          showResp("Система правил '"+(rn===""?"noName":rn)+"' удалена");
      }
    }
  }
  //let url = _G.prefix+"delRules.jsp";
  let url = _G.prefix+"/rules/delete";
  // xmlhttp.open("POST",url,true);
  xmlhttp.open("DELETE",url,true);
  xmlhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
  let jD={rules:''};
  jD.rules=rN;
  let dataR=JSON.stringify(jD);
  xmlhttp.send(dataR);
  timeClear();
}
function rulesToJson(why){
  let sLst;
//  if(why)
    sLst={what:why,name:_G.rnamE.value,extension:[],lexic:[],syntax:[],template:_G.tplName};
//  else
//    sLst={what:0,lexic:[],syntax:[]};
  if(((t=_G.extTH.value).trim())!=="")
    sLst.extension.push(t);
  for(let i=0;i<_G.wR.length;i++){
    let rule=_G.wR[i];
    let r=[_G.gS[rule[0]]];
    for(let j=1;j<rule.length;j++)
      r.push(_G.gS[rule[j]]);
    if(_G.gP[rule[0]]===4){
      if(r.length>2)
        for(let j=2;j<r.length;j++)
          r[1]+=" "+r[j];
      if(r[0].indexOf("!")<0)
        sLst.lexic.push(_G.toFBrFree(r));
      else
        sLst.lexic.push(r);
    }else
      sLst.syntax.push(r);
  }
//  alert(JSON.stringify(sLst));
  return sLst;
}




function buildT(what){
  if(_G.builD.classList.toString().indexOf("grSp")>=0)
    return;
//  viewSwitch(1);
  _G.what=what;
  if(_G.rulesChng&&(_G.autoS===1))
    saveRules();
  let xmlhttp;
  if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp=new XMLHttpRequest();
  } else {// code for IE6, IE5
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(){
    if (xmlhttp.readyState===4 && xmlhttp.status===200){
      let resp=xmlhttp.responseText.trim();
      if(resp.indexOf("password")>0){
        stop();
        return;
      }
      if(_G.what===0){
        if(_G.dTime>100)
            showResp(resp);
        var f=true;
        var ll=document.getElementsByName("lang");
        for(var i=0;i<ll.length;i++){
          if(ll[i].checked&&(ll[i].getAttribute("value")==="js")){
            _G.ruN.classList.remove("grSp");
            _G.tsT.classList.remove("grSp");
            f=false;
            break;
          }
        }
        if(f){
          document.getElementById("dwnL").style.display="block";
          document.getElementById("dwn").style.display="none";
        }
      }else{
        _G.divAny.innerHTML=resp;
        if(what==5){//Подключение скриптов к табличному аниматору сканера
          
          var script = document.createElement('script');
          script.type = 'text/javascript';
          script.src=_G.prefix+"js/TCScannerAnimator.js";

          for(let i=0;i<document.head.children.length;i++) // проверяем не подключен ли скрипт TCScannerAnimator.js
          {
              if((document.head.children[i].toString()==="[object HTMLScriptElement]")&&((ind=document.head.children[i].src.indexOf("TCScannerAnimator"))>0))
              {
              document.head.children[i].parentNode.removeChild(document.head.children[i]); // если скрипт уже подключен, отвязываем его             
              }
          }

          document.head.appendChild(script); // покдлючаем скрипт
          scrTxtAnimator.style.display="block";
          
//Конец подключения скриптов табличного аниматора

        }else if(what==6){//Подключение скриптов к графовому аниматору сканера
          let f=true;
          for(let i=0;i<document.head.children.length;i++)
            if((document.head.children[i].toString()==="[object HTMLScriptElement]")&&((ind=document.head.children[i].src.indexOf("GCScannerAnimator"))>0)){
              f=false;
            }
          if(f){
          var script = document.createElement('script');
          script.type = 'text/javascript';
//этот скрипт пока только сообщает о том, что он загружен, но в него можно добавлять что угодно, 
//только это что угодно нужно привязывать к элементам аниматора
//пример такой привязки есть в функции runT
          script.src=_G.prefix+"js/GCScannerAnimator.js";
          document.head.appendChild(script);
          
//здесь можно таким же способом подключить библиотеку jQuery, только нужно предварительно 
//разместить ее в каталоге js (или в другом месте, но это место нужно указать и сделать доступным)
//Конец подключения скриптов графового аниматора
          } 
        }
      }
    }
  }
  let sLst=rulesToJson(what);
//  _G.extTH.value=JSON.stringify(sLst);
  //let url = _G.prefix+"work/buildTranslator.jsp";
  let url = _G.prefix+"work/buildTranslator";
  xmlhttp.open("POST",url,true);
  xmlhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
  xmlhttp.send(JSON.stringify(sLst));
  
  timeClear();
}
function tuneSave(){
  // let js={tune:{language:"",scanner:"",parser:"",saving:"",deltat:0,assist:0}};
  let js={language:"",scanner:"",parser:"",saving:"",deltat:0,assist:0};
  let i,e,f;
  i=1;
  while(((e=document.getElementById("L_"+i))!==undefined)&&(e!==null)){
    if(e.checked){
      //js.tune.scanner=e.value;
      js.language=e.value;
      f=e.value;
      break;
    }
    i+=1;
  }
  i=1;
  while(((e=document.getElementById("s_"+i))!==undefined)&&(e!==null)){
    if(e.checked){
      //js.tune.scanner=e.value;
      js.scanner=e.value;
      f+="/"+e.value;
      break;
    }
    i+=1;
  }
  i=1;
  while(((e=document.getElementById("p_"+i))!==undefined)&&(e!==null)){
    if(e.checked){
      //js.tune.parser=e.value;
      js.parser=e.value;
      f+=e.value;
      break;
    }
    i+=1;
  }
  _G.tplName=f;
  i=1;
  while(((e=document.getElementById("autoS"+i))!==undefined)&&(e!==null)){
    if(e.checked){
      //js.tune.saving=(document.getElementById("saveNN").checked?"+":"-")+e.value;
      js.saving=(document.getElementById("saveNN").checked?"+":"-")+e.value;
      _G.autoS=parseInt(e.value);
      _G.saveNN=document.getElementById("saveNN").checked?true:false
      break;
    }
    i+=1;
  }
  i=parseInt(document.getElementById("deltaT").value);
  if((i!==null)&&(i!==undefined)&&(i>=100)&&(i<=10000))
    //js.tune.deltat=i;
    js.deltat=i;
  else
    i=0;
  document.getElementById("deltaT").value=i;
  if(document.getElementById("hlp1").checked)
    //js.tune.assist=1;
    js.assist=1;
  _G.helP=js.assist;
  viewSwitch(1);
  if(!_G.mongo)
    return;
  let xmlhttp;
  if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp=new XMLHttpRequest();
  } else {// code for IE6, IE5
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  xmlhttp.onreadystatechange=function(){
    if (xmlhttp.readyState===4 && (xmlhttp.status===200 && xmlhttp.status===201)){
      let re = xmlhttp.responseText.trim();
      if(re.charAt(0)==="<"){
        stop();
        return;
      } else if("Error"===re.substring(0,5))
        alert("Не удалось сохранить настройки: " + xmlhttp.responseText.substring(7));
      else
      if((_G.what===0)&&(_G.dTime>100))
        showResp(re);
    }
  }
  //let url = _G.prefix+"saveSettings.jsp";
  let url = _G.prefix+"/saveSettings";
  xmlhttp.open("POST",url,true);
  xmlhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
  let dataR=JSON.stringify(js);
  xmlhttp.send(dataR);
  timeClear();
}/*
function fenceOver(){
  let t=document.getElementById("shift");
  t.style.display="block";
  let cr=_G.tabR.getBoundingClientRect();
  t.style.top=_G.tabR.scrollTop+cr.top+"px";
  t.style.left=_G.tabR.scrollLeft+cr.left+"px";
}
function toLeft(){
  var td=document.getElementById("vidR");
  var w=parseInt(td.style.width);
  if(w>15){
    td.style.width=(w-5)+"%";
    fenceOver()
  }
}
function toRight(w=5){
  var td=document.getElementById("vidR");
  var w=parseInt(td.style.width);
  if(w<85){
    td.style.width=(w+5)+"%";
    fenceOver();
  }
}
function setFence(){
  let tblR=document.getElementById("factR");
  let cr=tblR.getBoundingClientRect();
  _G.divFnc.style.top=tblR.scrollTop+cr.top+"px";
  _G.divFnc.style.left=tblR.scrollLeft+cr.left+"px";
  if(document.getElementById("shift").style.display==="block")
    fenceOver()
}*/
function colHide(){
  var i=event.target.cellIndex;
  var j=1-i;
  var ta,tb;
  if(i===0){
    ta=_G.tabR;
    tb=_G.rnorM;
  }else{
    ta=_G.rnorM;
    tb=_G.tabR;
  }
  var tblR=document.getElementById("tblRules");
  if((tb.style.display!=="none")&&(ta.style.display!=="none")){
    tb.style.display="none";
    tblR.rows[0].cells[j].innText=tblR.rows[0].cells[j].innerText;
    tblR.rows[0].cells[j].innerText=" ";
    tblR.rows[0].cells[i].style.width="99%";
    tblR.rows[1].cells[i].style.width="99%";
    tblR.rows[0].cells[j].style.width="1%";
    tblR.rows[1].cells[j].style.width="1%";
  }else if(ta.style.display!=="none"){
    tblR.rows[0].cells[i].style.width="50%";
    tblR.rows[1].cells[i].style.width="50%";
    tblR.rows[0].cells[j].style.width="50%";
    tblR.rows[1].cells[j].style.width="50%";
    tb.style.display="block";
    tblR.rows[0].cells[j].innerText=tblR.rows[0].cells[j].innText;
//    document.getElementById("tblRules").rows[0].cells[j].innerText=" ";    
  }
  /*
  var j=1-i;
  var tblR=document.getElementById("tblRules");
  var toHid=true;
  if(tblR.rows[0].cells[j].style.width!=="50%")
    toHid=false;
  if(toHid){
    tblR.rows[0].cells[i].style.width="0%";
    tblR.rows[1].cells[i].style.width="0%";
    tblR.rows[0].cells[j].style.width="100%";
    tblR.rows[1].cells[j].style.width="100%";
  }else{
    tblR.rows[0].cells[i].style.width="50%";
    tblR.rows[1].cells[i].style.width="50%";
    tblR.rows[0].cells[j].style.width="50%";
    tblR.rows[1].cells[j].style.width="50%";
  }*/
}
function tplChng(a){
  if(a===undefined)
    a=event.target;
  if((a.getAttribute("value")!==null)&&(a.checked)){
    var tl;
    var me=a.name;
    var part=a.getAttribute("value");
    var ot=(me==='lex'?"synt":"lex");
    var ll=document.getElementsByName("lang");
    for(var i=0;i<ll.length;i++)
      if(ll[i].checked){
        tl=ll[i].parentElement.getAttribute("tList");
        break;
      }
    ot=document.getElementsByName(ot);
    for(var i=0;i<ot.length;i++){
      var nm=(me=="lex"?part+ot[i].getAttribute("value"):ot[i].getAttribute("value")+part);
      if(tl.indexOf(nm)>=0)
        ot[i].disabled="";
      else{
        ot[i].checked="";
        ot[i].disabled="disabled";
      }
    }
  }
}
function langChng(a){
  if(a===undefined)
    a=event.target;
  if(a.getAttribute("value")!==null){
    if(a.getAttribute("lang")==="js")
      _G.ruN.classList.remove("grSp");
    else
      _G.ruN.classList.add("grSp");
    var tl=a.parentElement.getAttribute("tList");
    var el=document.getElementsByName("lex");
    for(var i=0;i<el.length;i++){
      if(tl.indexOf(el[i].getAttribute("value"))>=0)
        el[i].disabled="";
      else{
        el[i].disabled="disabled";
        el[i].checked="";
      }
    }
    var el=document.getElementsByName("synt");
    for(var i=0;i<el.length;i++){
      var val=el[i].getAttribute("value");
      if(tl.indexOf(val)>=0)
        el[i].disabled="";
      else{
        el[i].disabled="disabled";
        el[i].checked="";
      }
    }
  }
}
function actionsS_H(){
  _G.mode=!_G.mode;
  _G.showRules();
}
function rulesSort(){
  if(_G.tR.length===0)
    return;
  var ntR=[];
  var ltR=[];
  let rNorm=_G.rnorM;
  let nt="";
  for(var i=1;i<rNorm.rows.length;i++){
    if(nt!==rNorm.rows[i].cells[0].innerText.trim()){
      nt=rNorm.rows[i].cells[0].innerText.trim();
      for(var j=0;j<_G.tR.length;j++){
        var k=_G.tR[j].indexOf(" ");
        var lf=(k<0?_G.tR[j]:_G.tR[j].substring(0,k));
        if(lf===nt)
          if(_G.gP[_G.gS.indexOf(lf)]===4)
            ltR.push(_G.tR[j]);
          else
            ntR.push(_G.tR[j]);
      }
    }
  }
  for(var i=0;i<ltR.length;i++)
    ntR.push(ltR[i]);
  _G.tR=ntR;
  _G.showRules();
}
function timer(){
  if(_G.limit>60)
    _G.limit-=60;
  if((_G.limit<180)&&(_G.limit>60)){
    document.getElementById("flame2").style.backgroundColor="yellow";
    if((_G.limit===120)&&_G.rulesChng&&(_G.autoS===3))
      saveRules();
  }
  if(_G.limit===60)
    _G.alarmI=setInterval(alarm, 1000);
}
function alarm(){
  if(--_G.limit<=20){
    document.getElementById("flame1").style.backgroundColor="#eeeeee";
    if(_G.limit%2===0){
      document.getElementById("flame2").style.backgroundColor="yellow";
      document.getElementById("flame3").style.backgroundColor="#eeeeee";
    }else{
      document.getElementById("flame2").style.backgroundColor="#eeeeee";
      document.getElementById("flame3").style.backgroundColor="red";
    }
  }
  if(_G.limit<0){
    document.getElementById("flame3").style.backgroundColor="red";
    document.getElementById("flame2").style.backgroundColor="#eeeeee";
    clearInterval(_G.alarmI);
    clearInterval(_G.timerI);
  }
}
function timeClear(){
  _G.limit=1860;
  document.getElementById("flame3").style.backgroundColor="#eeeeee";
  document.getElementById("flame2").style.backgroundColor="#eeeeee";
  document.getElementById("flame1").style.backgroundColor="#00ff00";
//  _.timer();
}
function helpOpen(){
  if(_G.helP===1)
    window.open(_G.prefix+"/help");
    //window.open(_G.prefix+"help/WBChelp.jsp");
  else {
    viewSwitch(5);
    if(_G.ifr.src==="")
      _G.ifr.src=_G.prefix+"/help";
      //_G.ifr.src=_G.prefix+"help/WBChelp.jsp";
  }
}
function showTime(w){
  let vt=document.getElementById("viewT");
  let tim=(_G.limit-60)/60;
  if(tim<0)
    tim=0;
  vt.innerHTML="<span>"+tim+"&nbsp;мин.</span>";
  if(w&&(vt.style.display!=="block")){
    let ev=window.event;
    vt.style.left=ev.clientX+10+"px";
    vt.style.top=ev.clientY+10+"px";
    vt.style.display="block";
  }else
    vt.style.display="none";
}
function vJob(v){
  let dj=document.getElementById("dJob");
  if(v){
    if(dj.style.display!=="block"){
      let vr=document.getElementById("asUser").innerText.trim();
      if(vr=="000000000")
        vr="111111111";
      if(dj.asUser!==vr){
        dj.asUser=vr;
        dj.style.display="block";
        let h="",n,f;
        for(let i=0;i<vr.length;i++){
          n=""+(i+1)+vr.substring(i,i+1)+".";
          f=true;
          for(let j=0;j<_G.job.length;j++)
            if(_G.job[j].indexOf(n)===0){
              h+="<tr><td>"+_G.job[j].substring(1);
              f=false;
              break;
            }
          if(f)
            return;
        }
        document.getElementById("tJob").innerHTML=h;
        dj.style.left=_G.divExt.offsetWidth-dj.offsetWidth+100+"px";
        dj.style.top="5px";
      }else if(dj.style.display!=="block")
        dj.style.display="block";
    }
  }else
    dj.style.display="none";
}
