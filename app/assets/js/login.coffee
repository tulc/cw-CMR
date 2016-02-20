$ ->
  $('#email').addClass('form-control')
  $('#email').removeAttr('type')
  $('#email').attr('placeholder','Email address')
  $('#email').attr('type','email')
  $('#email').parent().find('.fa').addClass('fa-envelope-o')
  $('#password').addClass('form-control')
  $('#password').attr('placeholder','Password')
  $('#password').parent().find('.fa').addClass('fa-key')