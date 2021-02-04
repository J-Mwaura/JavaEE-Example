$(document).ready(function(){
   function replace_content(content)
   {
   var exp_match = /(\b(https?|):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
   var element_content=content.replace(exp_match, "<a href='$1'>$1</a>");
   var new_exp_match =/(^|[^\/])(www\.[\S]+(\b|$))/gim;
   var new_content=element_content.replace(new_exp_match, '$1<a target="_blank" href="http://$2">$2</a>');
   return new_content;
   }
   var content = $('#content').html();
   $('#content').html(replace_content(content));
});