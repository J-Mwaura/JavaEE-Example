/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var inputText;
function onLoad(){  
            inputText = document.getElementById('username'); 
             
      } 
      function onFocus(){
        alert(inputText.value);   
       }


