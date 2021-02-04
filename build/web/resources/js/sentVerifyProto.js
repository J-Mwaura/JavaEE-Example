/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
$(document).ready(function() {

    var fullname = $('#fullname');
    var fullname_warn = $('#fullname_warn');

    var email = $('#email');
    var email_warn = $('#email_warn');

    fullname.focus(function(){
        fullname_warn.removeClass('goodtogo');
        fullname_warn.addClass('warning');
        fullname_warn.html("Cannot be blank");

        fullname.blur(function(){
            fullname_warn.removeClass('warning');
            fullname_warn.addClass('goodtogo');
            fullname_warn.html(" ");
        });
    });

    email.focus(function(){
        email_warn.removeClass('goodtogo');
        email_warn.addClass('warning');
        email_warn.html("Cannot be blank");

        email.blur(function(){
            email_warn.removeClass('warning');
            email_warn.addClass('goodtogo');
            email_warn.html(" ");
        });
    });

});

