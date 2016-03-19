$ ->
  $('#btnDelete').click ->
    comment = $('#txt-comment').val().trim()
    $('#modal-notify').find('.modal-header').empty()
    $('#modal-notify').find('.modal-body').empty()
    $('#modal-notify').find('.modal-footer').empty()
    if comment == ''
      $('#modal-notify').modal({backdrop:false,keyboard:false})
      $('#modal-notify').find('.modal-header').append('<h4 class="modal-title">Notification</h4>')
      $('#modal-notify').find('.modal-body').append('<p>Please write some comment about your cmr report before you reject.</p>')
      $('#modal-notify').find('.modal-footer').append('<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>')
      $('#modal-notify').modal('show')
    else
      $('#btnDelete').attr('disabled','disabled')
      $('#btnSubmit').attr('disabled','disabled')
      $('#confirm-delete').modal('hide')
      $.ajax url:$(this).data('href'),
      data: {comment: comment},
      type: 'POST',
      success: (data) ->
        if data == '1'
          $('#modal-notify').modal({backdrop:false,keyboard:false})
          $('#modal-notify').find('.modal-body').append('<p>The course monitoring report has been delete. The page will redirect to reports page in 3 seconds</p>')
          $('#modal-notify').find('.modal-footer').append('<a href="/cmr-reports" class="btn btn-primary">View reports now</a>')
          $('#modal-notify').modal('show')
          redirect = () -> location.href = '/cmr-reports'
          setTimeout(redirect,3000)
        else
          $('#btnDelete').removeAttr('disabled')
          $('#btnSubmit').removeAttr('disabled','disabled')
          alert("Error")

  $('#btnSubmit').click ->
    comment = $('#txt-comment').val().trim()
    $('#modal-notify').find('.modal-header').empty()
    $('#modal-notify').find('.modal-body').empty()
    $('#modal-notify').find('.modal-footer').empty()
    if comment == ''
      $('#modal-notify').modal({backdrop:false,keyboard:false})
      $('#modal-notify').find('.modal-header').append('<h4 class="modal-title">Notification</h4>')
      $('#modal-notify').find('.modal-body').append('<p>Please write some comment about your cmr report before you submit.</p>')
      $('#modal-notify').find('.modal-footer').append('<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>')
      $('#modal-notify').modal('show')
    else
      $('#btnSubmit').attr('disabled','disabled')
      $('#btnDelete').attr('disabled','disabled')
      $.ajax url:$(this).data('href'),
      data: {comment: comment},
      type: 'POST',
      success: (data) ->
        if data == '1'
          $('#modal-notify').modal({backdrop:false,keyboard:false})
          $('#modal-notify').find('.modal-header').append('<h4 class="modal-title">Success</h4>')
          $('#modal-notify').find('.modal-body').append('<p>The course monitoring report has been submited. The page will redirect to reports page in 3 seconds</p>')
          $('#modal-notify').find('.modal-footer').append('<a href="/cmr-reports" class="btn btn-primary">View reports now</a>')
          $('#modal-notify').modal('show')
          redirect = () -> location.href = '/cmr-reports'
          setTimeout(redirect,3000)
        else
          $('#btnDelete').removeAttr('disabled')
          $('#btnSubmit').removeAttr('disabled')
          alert("Error")