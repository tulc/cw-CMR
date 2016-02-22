$ ->
  $('#btnDelete').click ->
    $(this).attr('disabled','disabled')
    $('#btnSubmit').attr('disabled','disabled')
    $('#confirm-delete').modal('hide')
    $.ajax url:$(this).data('href'),
    type: 'POST',
    success: (data) ->
      if data == '1'
        $('#modal-notify').modal({backdrop:false,keyboard:false})
        $('#modal-notify').find('.modal-body').append('<p>The course monitoring report has been delete. The page will redirect to reports page in 3 seconds</p>')
        $('#modal-notify').find('.modal-footer').append('<a href="/reports" class="btn btn-primary">View reports now</a>')
        $('#modal-notify').modal('show')
        redirect = () -> location.href = '/reports'
        setTimeout(redirect,3000)
      else
        $('#btnDelete').removeAttr('disabled')
        $('#btnSubmit').removeAttr('disabled','disabled')
        alert("Error")

  $('#btnSubmit').click ->
    $(this).attr('disabled','disabled')
    $('#btnDelete').attr('disabled','disabled')
    $.ajax url:$(this).data('href'), type: 'POST',
    success: (data) ->
      if data == '1'
        $('#modal-notify').modal({backdrop:false,keyboard:false})
        $('#modal-notify').find('.modal-body').append('<p>The course monitoring report has been submited. The page will redirect to reports page in 3 seconds</p>')
        $('#modal-notify').find('.modal-footer').append('<a href="/courses" class="btn btn-primary">View reports now</a>')
        $('#modal-notify').modal('show')
        redirect = () -> location.href = '/reports'
        setTimeout(redirect,3000)
      else
        $('#btnDelete').removeAttr('disabled')
        $('#btnSubmit').removeAttr('disabled')
        alert("Error")