function ruleParser(){
  this.curLXM = [];
  this.stk = [];
  this.txt = "";
  this.len = 0;
  this.pos = 0;

this.lStates = {"s0":[-3,/(\p{L}|\d|[_$])+/u,1,"\"",4,"|",5,"+*?",6,"{",14,"[",17," \u00A0\t",18,"(",19,")",20,":",21,"~",22,"^",-14,""],
  "s1":[2,"\\",3,"\"",1,""], "s2":[1,""], "s3":[-7,""], "s4":[-6,""],"s5":[-11,""],
  "s6":[7,"0-9",10,",",12,""], "s7":[7,"0-9",9,",",8,"}"],"s8":[-11,""],
  "s9":[11,"0-9",8,"}"], "s10":[11,"0-9"],"s11":[11,"0-9",8,"}"],
  "s12":[13,"}", 12,""],"s13":[-9,""],"s14":[15,"\\",16,"]",14,""],"s15":[14,""],
  "s16":[-8,""],"s17":[17," \u00A0\t",0,""],"s18":[-4,""],"s19":[-5,""],
  "s20":[-12,""],"s21":[-10,""],"s22":[-13,""]};
//-3  1  id
//-4  2  ~          (				4->2	-6->-4
//-5  3  action			)				5->3	-7->-5
//-6  4  (          |				10->4	-12->-6
//-7  5  )          "..." string	7->5	-9->-7
//-8  6  [ ... ] RE		[ ... ] RE		
//-9  7  "..." string	action			3->7	-5->-9
//-10 8  :				~				2->8	-4->-10
//-11 9  kvantifikator	kvantifikator	
//-12 10 |				:				8->10	-10->-12
//-13    ^
//-14 11 error
const fEM = 0x80000000;
const fAM = 0x40000000;
const fSM = 0x20000000;
const fRM = 0x10000000;

  this.states = [31,34|fEM,35,37|fEM,38|fEM,42,44|fEM,45|fEM,46,47|fEM,48,49|fEM,50,51,54|fEM,57,58,61|fEM,64,65|fEM,
    66,67|fEM,68,69|fEM,72,73|fEM,76,77|fEM,80,81|fEM,84,32|fAM,9|fSM,11,13,36|fAM,37|fAM|fRM,38|fAM|fRM,39|fAM,
    16|fSM,41|fAM,19,6|fSM,21,45|fAM|fRM,46|fAM|fRM,47|fAM|fRM,48|fAM|fRM,49|fRM,1,51|fRM,3|fSM,23|fSM,14,3|fSM,
    25|fSM,14,58|fRM,3|fSM,27|fSM,17,3|fSM,29|fSM,17,65|fRM,66|fAM|fRM,67|fRM,68|fAM|fRM,69|fRM,70|fAM,3|fSM,
    23,73|fRM,74|fAM,3|fSM,25,77|fRM,78|fAM,3|fSM,27,81|fRM,82|fAM,3|fSM,29,85|fRM,0xffffffff];
};
ruleParser.prototype = {
getLexem:function () {
  while(true){
    let start = this.pos;
    if(start>=this.len)
      return [0,""];
    let noState = 0;
    let token = -1;
    while(this.pos<=this.len) {
      let c = this.txt[this.pos] || "\u0000";
      let state = this.lStates["s"+noState];
      token = 0;
      for(let i = 0; i < state.length; i+=2){
        let mark = state[i+1];
        if(typeof(mark)==="object"){
          let tok = this.txt.substring(this.pos).match(mark);
          if((tok !== null) && (tok.index === 0)) {
            this.pos += tok[0].length;
            token = state[i];
            break;             
          }
        } else {
          if((mark.length===3) && (mark[1]==="-")){
            if((c>=mark[0]) && (c<=mark[2])) {
              token = state[i];
              break;
            }
          } else if((mark === "") || (mark.indexOf(c) >=0)) {
            token = state[i];
            break;
          }
        }
      }
      if(token <= 0)
        break;
      this.pos += 1;
      noState = token;
    }
    if(token===-1)
      return([0,""]);
    else if(token < -2)
      return([-2-token,this.txt.substring(start,this.pos)]);//
    else if(token!==0)
      return([-999,""]);
  }
},
parse: function(txt){
const fEM = 0x80000000;
const fAM = 0x40000000;
const fSM = 0x20000000;
const fRM = 0x10000000;
  this.txt = txt;
  this.len = txt.length;
  this.pos = 0;
  this.stk = [];
  let rez = [];
  let cWI;
  let nextState;
  let sM = 0xfffffff;
  let sS=[2,230,256,128,4,98,34,64,32,1024,487,486,1,230,230,1,230,230,8,512,255,512,255,16,231,16,231,16,238,16,238];
  this.curLXM = this.getLexem();
  if((cWI = this.curLXM[0]) < 0)
    return {"Ok":false,"rez":[]};
  rez.push(this.curLXM);
  let curSt=0;
  while(true){
    if(curSt >= this.states.length)
        break;
    nextState = this.states[curSt];
    let frez={"Ok":false,"rez":rez};
    switch (curSt){
       case 31:if(cWI!==1)return frez;break
       case 35:if(cWI!==8)return frez;break; 
       case 36:if((cWI!==1)&&(cWI!==5))return frez;break;
       case 37:if(cWI!==7)return frez;break;
       case 38:if(cWI!==2)return frez;break;
       case 40:if(cWI!==3)return frez;break;
       case 44:if((cWI!==1)&&(cWI!==5))return frez;break;
       case 45:if(cWI!==6)return frez;break;
       case 46:if(cWI!==5)return frez;break;
       case 47:if(cWI!==10)return frez;break;
       case 48:break;
       case 50:break;
       case 57:break;
       case 64:break;
       case 65:if(cWI!==9)return frez;break;
       case 66:break;
       case 67:if(cWI!==9)return frez;break;
       case 68:break;
       case 69:if(cWI!==4)return frez;break;
       case 72:break;
       case 73:if(cWI!==4)return frez;break;
       case 76:break;
       case 77:if(cWI!==4)return frez;break;
       case 80:break;
       case 81:if(cWI!==4)return frez;break;
       case 84:break;
    }
    if(curSt<31)
        curSt=((sS[curSt]&(1<<cWI))? nextState & sM:((nextState & fEM) === 0? 0xffffffff: curSt + 1)); 
    else{
        if((nextState & fAM)!==0){
          this.curLXM = this.getLexem();
          if((curSt!==31)||(this.curLXM[0]!==10)) //подавление двоеточия после нетерминала в левой части
             //обработка символа ^
            if(this.curLXM[0]===11){
              rez.push(this.curLXM);
              this.curLXM = this.getLexem();
              if(this.curLXM[0]!==6){
                rez.push(this.curLXM);
                return {"Ok":false,"rez":rez};
              }
            }
            rez.push(this.curLXM);
          if((cWI = this.curLXM[0]) < 0)
                break;
        }
        if((nextState & fSM) !== 0)
            this.stk.push(curSt + 1);
        if((nextState & fRM) !== 0)
            if(this.stk.length > 0)
                curSt = this.stk.pop();
            else
                break;
        else
            curSt=nextState & sM;
    }
  }
  if(rez[rez.length-1][1]==="")
    rez.pop();
  return({"Ok":(((cWI === 0) && (this.stk.length===0))? true : false),"lRule":rez});
 }
};