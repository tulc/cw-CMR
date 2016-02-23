$ ->
  $('#email').addClass('form-control')
  $('#email').attr('placeholder','Email address or Guest account')
  $('#email').parent().find('.fa').addClass('fa-envelope-o')
  $('#password').addClass('form-control')
  $('#password').attr('placeholder','Password')
  $('#password').parent().find('.fa').addClass('fa-key')