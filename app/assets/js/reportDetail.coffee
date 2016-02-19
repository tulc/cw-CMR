$ ->
  $('#btnRemove').click ->
    $(this).attr('disabled','disabled')
    $('#confirm-delete').modal('hide')
    $.ajax url:$(this).data('href'),
    type: 'DELETE',
    success: (data) ->
      if data == '1'
        $('#modal-notify').modal({backdrop:false,keyboard:false})
        $('#modal-notify').modal('show')
        redirect = () -> location.href = '/courses'
        setTimeout(redirect,3000)
      else
        $('#btnRemove').removeAttr('disabled')
        alert("Error")