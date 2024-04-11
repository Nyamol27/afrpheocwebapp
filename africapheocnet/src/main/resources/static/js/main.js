 function showAlert(type, message) {
        var alertBox = $('#alertBox');
        var alertMessage = alertBox.find('.alert-message');
        var alertIcon = alertBox.find('.uil');

        alertMessage.text(message);

        if (type === 'success') {
            alertBox.removeClass('alert-warning').addClass('alert-success');
            alertIcon.removeClass('uil-exclamation-triangle').addClass('uil-check-circle');
        } else {
            alertBox.removeClass('alert-success').addClass('alert-warning');
            alertIcon.removeClass('uil-check-circle').addClass('uil-exclamation-triangle');
        }

        alertBox.show();

        setTimeout(function() {
            alertBox.hide();
        }, 10000); 
    }

// Send Invitation
function sendInvite() {
    // Obtain CSRF token from meta tags
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Get email and name from the input fields
    var email = $('#inputEmail').val();
    var name = $('#inputName').val(); 

    // Prepare data to be sent
    var requestData = {
        email: email,
        name: name 
    };

    // Add CSRF token to the headers
    var headers = {};
    headers[csrfHeader] = csrfToken;

    // Create AJAX request
    $.ajax({
        type: 'POST',
        url: '/invitations/send',
        data: requestData, 
        beforeSend: function(xhr) {
            // Set CSRF token in the request header
            xhr.setRequestHeader(csrfHeader, csrfToken);
        },
        success: function(response) {
            if (response === 'Invitation sent successfully.') {
                // Display a success message
                showAlert('success', 'Invite sent successfully.');
                // Clear the form fields
                $('#inputEmail').val('');
                $('#inputName').val('');
            } else {
                // Display an error message from the response
                showAlert('error', 'Failed to send invite: ' + response);
            }
        },
        error: function(xhr, status, error) {
            // Handle AJAX errors
            showAlert('error', 'Failed to send invite. Please try again later.');
        }
    });
}

// JavaScript code for handling search functionality
$(document).ready(function() {
	
	
    function bindProfileButtonListeners() {
        $('.profile-button').click(function() {
            var email = $(this).data('email');
        });
    }
    // Handle form submission for search
    $('#searchForm').submit(function(event) {
        // Prevent the default form submission behavior
        event.preventDefault();
        
        // Get the search query from the input field
        var query = $('#searchInput').val().trim();
        
        // Check if the search query is empty
        if (query === '') {
            // Show an alert message indicating that the search query cannot be empty
            showAlert('error', 'Please enter a search query.');
            return; // Exit the function if the search query is empty
        }

        // Get the CSRF token value from the meta tag
        var csrfToken = $('meta[name=_csrf]').attr('content');

        // Make an AJAX request to search for members
        $.ajax({
            url: '/members/search', // Update the URL with your backend endpoint for searching
            method: 'GET',
            headers: {
                'X-CSRF-TOKEN': csrfToken // Include CSRF token in the request header
            },
            data: { query: query }, // Pass the search query as data
            success: function(response) {
    console.log('Received backend response:', response);
    if (response.length === 0) {
        // No members found, display a message to the user
        showAlert('warning', 'No members found matching your search query.');
    } else {
        // Clear the existing member list container
        $('#memberListContainer').empty();

        // Iterate over the response array and append member elements
response.forEach(function(member) {
    var memberElement = '<div class="col-xl-4 col-sm-6">';
    memberElement += '<div class="card">';
    memberElement += '<div class="card-body p-4">';
    memberElement += '<div class="d-flex align-items-start">';

    // Check if member.base64Photo is not null
    if (member.base64Photo != null) {
        memberElement += '<div class="flex-shrink-0 avatar rounded-circle me-3">';
        memberElement += '<img src="data:image/jpg;base64,' + member.base64Photo + '" alt="Member Photo" class="img-fluid rounded-circle">';
        memberElement += '</div>';
    } else {
        // If member.base64Photo is null, display a placeholder image or initials
        memberElement += '<div class="rounded-circle me-3" style="width: 50px; height: 50px; background-color: #9370DB; color: white; text-align: center; line-height: 50px;">';
        memberElement += '<span>' + member.firstName[0] + member.lastName[0] + '</span>';
        memberElement += '</div>';
    }

    // Continue building the member element
    memberElement += '<div class="flex-grow-1 overflow-hidden">';
    memberElement += '<h5 class="font-size-15 mb-1 text-truncate">';
    memberElement += '<a href="#" class="text-reset">' + member.firstName + ' ' + member.lastName + '</a>';
    memberElement += '</h5>';
    memberElement += '<span class="badge bg-success-subtle text-success mb-0">' + member.expertise + '</span>';
    memberElement += '<span class="badge bg-success-subtle text-info mb-0">' + member.nationality + '</span>';
    memberElement += '</div>';
    memberElement += '</div>';
    memberElement += '<br>';
    memberElement += '<div class="pt-2">';
    memberElement += '<button type="button" class="btn btn-soft-primary btn-sm w-md text-truncate" onclick="location.href=\'mailto:' + member.email + '\'">';
    memberElement += '<i class="bx bx-user me-1 align-middle"></i> Contact';
    memberElement += '</button>';
    memberElement += '<button type="button" class="btn btn-primary btn-sm w-md text-truncate ms-2 profile-button" th:data-email="' + member.email + '">';
    memberElement += '<i class="bx bx-message-square-dots me-1 align-middle"></i> Profile';
    memberElement += '</button>';
    memberElement += '</div>';
    memberElement += '</div>';
    memberElement += '</div>';
    memberElement += '</div>';

    $('#memberListContainer').append(memberElement);
});
 bindProfileButtonListeners();
    }
},
            error: function(xhr, status, error) {
                console.error('AJAX Error: ' + error);
                alert('Failed to perform the search. Please try again later.');
            }
        });
    });
    
   // Handle click event for the Reset button
		$('#reset-button').click(function() {
		    // Clear the search input field
		    $('#searchInput').val('');
		    
		    // Reload the current page
		    location.reload();
		});


    
});



// Feedback function
function checkEntry() {
  // Get the values entered by the user
  const rating = document.querySelector('input[name="rating"]:checked');
  const type = document.getElementById('inputCategory').value;
  const feedback_text = document.getElementById('inputFeedback').value;

  // Get the alert box element
  const alertBox = document.getElementById('alertBox');

  // Validate CSRF token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

  // Check if any of the fields are empty
  if (!rating || type === "--- Please choose ---" || feedback_text.trim() === "") {
    // Display an error message in the alert
    showAlert(alertBox, "Please fill in all the required fields.", "alert-warning");
  } else {
    // Prepare the data to send via AJAX
    const data = {
      rating: rating.value,
      type: type,
      feedbackText: feedback_text,
    };

    // Perform the AJAX call
    fetch('/feedback/new', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken,
      },
      body: JSON.stringify(data),
    })
      .then(response => {
        if (response.ok) {
          // Handle success
          showAlert(alertBox, "Feedback submitted successfully.", "alert-success");

          // Clear the form inputs
          clearFormInputs();
        } else {
          // Handle error
          showAlert(alertBox, "Error submitting feedback.", "alert-warning");
        }
      })
      
  }
}



// Function to clear form inputs
function clearFormInputs() {
  document.getElementById('inputCategory').value = "--- Please choose ---";
  document.querySelector('input[name="rating"]:checked').checked = false;
  document.getElementById('inputFeedback').value = "";
}




function confirmAction(action, userEmail) {
    // Fetch CSRF token from the server
    fetch('/csrf-token-endpoint')
        .then(response => response.json())
        .then(data => {
            const csrfToken = data.csrfToken;


            if (confirm(`Are you sure you want to ${action} the user with email: ${userEmail}?`)) {
                // User confirmed, call the respective function in the controller
                performAction(action, userEmail, csrfToken);
            } else {
                // User canceled the action
                alert('Action canceled.');
            }
        })
        .catch(error => console.error('Error fetching CSRF token:', error));
}


function performAction(action, userEmail, csrfToken) {
    // Perform AJAX request to the controller with CSRF token
    $.ajax({
        type: 'POST', 
        url: `/users/admin/${action}`, 
        data: { email: userEmail },
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        success: function (response) {
            // Handle success
            alert(`${action} successful.`);
        },
        error: function (error) {
            // Handle error
            alert(`${action} failed. ${error.responseText}`);
        }
    });
}

// Export member list 

   function exportToExcel() {
        // Make an AJAX request to the exportData endpoint
        $.ajax({
            type: "GET",
            url: "/members/export",  
            xhrFields: {
                responseType: 'blob' 
            },
            success: function(data, status, xhr) {
                // Handle the success response
                console.log("Export successful:", data);

                // Create a Blob from the data received
                var blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

                // Create a link element to trigger the download
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = 'members_list.xlsx';

                // Append the link to the body and trigger the click event
                document.body.appendChild(link);
                link.click();

                // Remove the link from the body
                document.body.removeChild(link);
            },
            error: function(error) {
                // Handle the error response
                console.error("Export failed:", error);
            }
        });
    }
    
// Add event listener to the document to capture clicks on elements with class 'delete-contact'
document.addEventListener('click', function(event) {
    // Check if the clicked element has the class 'delete-contact'
    if (event.target.classList.contains('delete-contact')) {
        // Retrieve the value of the data-contact-id attribute from the clicked element
        var contactId = event.target.getAttribute('data-contact-id');
        
        // Call the function to confirm and delete the contact
        confirmDeleteContact(contactId);
    }
});

// Adjusted function to confirm and delete a contact
function confirmDeleteContact(contactId) {
    const isConfirmed = window.confirm("Are you sure you want to delete this contact?");
    if (isConfirmed) {
        // If the user confirms the deletion
        deleteContact(contactId);
    } else {
        console.log('Delete canceled');
    }
}

// Function to perform an asynchronous request to delete the contact on the server
async function deleteContact(contactId) {
    try {
        // Retrieve CSRF token from meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Construct the request URL
        const url = `/contacts/delete/${contactId}`;

        // Make the fetch request
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken, // Set CSRF token in request headers
            },
            credentials: 'same-origin' // Include cookies in the request
        });

        // Check if the response indicates success
        if (response.ok) {
            // If successful, display a success message
            alert('Contact deleted successfully!');
            // Reload the page
            window.location.reload();
        } else {
            // If the response indicates an error, display the error message
            const data = await response.json();
            alert(data.message || 'Failed to delete contact.');
        }
    } catch (error) {
        // If an error occurs during the request
        alert('Failed to delete contact. Please try again later.');
        console.error('Error deleting contact:', error);
    }
}




// Function update contact details

// Function to validate and submit the form
function validateAndSubmit() {
    var form = $('#editContactForm')[0];
    if (form.checkValidity()) {
        // If the form is valid, check if the mobile number is a digit
        var mobileInput = $('#mobile').val();
        if (!(/^\d+$/.test(mobileInput))) {
            // If the mobile number is not a digit, show an error alert
            showAlert('error', 'Mobile number must contain only digits.');
            return; // Exit the function without submitting the form
        }
        // If the mobile number is a digit, proceed with form submission
        submitForm();
    } else {
        // If the form is invalid, show an alert
        showAlert('error', 'Please complete all fields and ensure the email is in the correct format.');
    }
}


function submitForm() {
    // Retrieve CSRF token from meta tags
    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Retrieve contact ID from the input field
    var contactId = $('#contactId').val();

    // Prepare data to be submitted
    var formData = $('#editContactForm').serialize();

    // Add CSRF token to headers
    var headers = {};
    headers[csrfHeader] = csrfToken;

    $.ajax({
        type: 'POST',
        url: '/contacts/update/' + contactId,
        data: formData,
        headers: headers,
        success: function (response) {
            showAlert('success', 'Contact updated successfully!');
        },
        error: function (error) {
            // Handle error, e.g., show an error message
            showAlert('error', 'Failed to update contact.');
        }
    });
}


// Export contact list to excel 

 $(document).ready(function () {
        // Click event for the Export to Excel button
        $('#exportExcelButton').on('click', function () {
            exportContactToExcel();
        });

        function exportContactToExcel() {
            // Make an AJAX request to the controller endpoint for exporting contacts to Excel
            $.ajax({
                type: 'GET',
                url: '/contacts/export',
                success: function (data) {
                    // Create a Blob from the response data
                    var blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

                    // Create a link element and trigger a download
                    var link = document.createElement('a');
                    link.href = window.URL.createObjectURL(blob);
                    link.download = 'contacts_export.xlsx';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                },
                error: function (error) {
                    // Handle error, e.g., show an error message
                    console.error('Failed to export contacts to Excel');
                }
            });
        }
    });
    
// Add new contact 

 function submitAddContactForm() {
    // Validate form fields
    var country = $('#addcontact-country-input').val();
    var firstName = $('#addcontact-firstname-input').val();
    var lastName = $('#addcontact-lastname-input').val();
    var department = $('#addcontact-department-input').val();
    var position = $('#addcontact-position-input').val();
    var mobile = $('#addcontact-mobile-input').val();
    var email = $('#addcontact-email-input').val();

    // Check if any field is empty
    if (!country || !firstName || !lastName || !department || !position || !mobile || !email) {
        showAlert('warning', 'Please fill in all required fields.');
        return;
    }

    // Validate email format
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('warning', 'Please enter a valid email address.');
        return;
    }

    // Obtain CSRF token from meta tags
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Prepare data to be sent
    var formData = $('#addContactForm').serialize();

    // Send data to the controller
    $.ajax({
        type: 'POST',
        url: '/contacts/add',
        data: formData,
        headers: {
            [csrfHeader]: csrfToken
        },
        success: function (response) {
            showAlert('success', 'Contact added successfully!');
             $('#addContactForm')[0].reset();
        },
        error: function (error) {
            showAlert('danger', 'Failed to add contact.');
        }
    });
}


    // Add New trainer 
    
   function submitAddTrainerForm() {
    // Serialize the form data
    var formData = $("#addTrainerForm").serialize();

    // Validate the form fields
    var firstName = $("#addTrainer-firstname-input").val();
    var lastName = $("#addTrainer-lastname-input").val();
    var gender = $("#addTrainer-gender-input").val();
    var country = $("#addTrainer-country-input").val();
    var position = $("#addTrainer-position-input").val();
    var organization = $("#addTrainer-organization-input").val();
    var email = $("#addTrainer-email-input").val();
    var telephone = $("#addTrainer-telephone-input").val();
    var language = $("#addTrainer-language-input").val();

    if (!firstName || !lastName || !gender || !country || !position || !organization || !email || !telephone || !language) {
       
        showAlert("warning", "All fields are required!");
        return;
    }

    // Validate email format
    var emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        showAlert("warning", "Invalid email format!");
        return;
    }

    // Validate phone number is a digit
    if (isNaN(telephone)) {
        showAlert("warning", "Phone number must be a digit!");
        return;
    }

    // CSRF token
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    // Send AJAX request
    $.ajax({
        type: "POST",
        url: "/roster/addTrainer",
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        data: formData,
        success: function(response) {
            showAlert("success", response);
            // Clear form fields on success
            $("#addTrainerForm")[0].reset();
        },
        error: function(xhr, status, error) {
            showAlert("warning", xhr.responseText);
        }
    });
}


    
// Delete a trainer
function confirmDeleteTrainer(trainerId) {
    var confirmation = confirm("Are you sure you want to delete this trainer?");
    if (confirmation) {
        // If the user confirms, call the deleteTrainer function with the trainerId
        deleteTrainer(trainerId);
    }
}

function deleteTrainer(trainerId) {
    // Use AJAX to call the deleteTrainer endpoint in your controller
    $.ajax({
        type: 'POST',
        url: '/roster/deleteTrainer', 
        data: { 'trainerId': trainerId },
        success: function (response) {
            // Check if the deletion was successful
            alert(response); // Alert the message from the server
            // Refresh the page after successful deletion
            location.reload();
        },
        error: function (error) {
            // Display error message
            alert("Error deleting trainer: " + error.responseText);
        }
    });
}


// Function to handle the exported data and create an Excel file (trainer)
function exportTrainerToExcel() {
        // Make an AJAX request to the exportData endpoint
        $.ajax({
            type: "GET",
            url: "/roster/exportTrainersToExcel",  
            xhrFields: {
                responseType: 'blob' 
            },
            success: function(data, status, xhr) {
                // Handle the success response
                console.log("Export successful:", data);

                // Create a Blob from the data received
                var blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

                // Create a link element to trigger the download
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = 'trainer_list.xlsx';

                // Append the link to the body and trigger the click event
                document.body.appendChild(link);
                link.click();

                // Remove the link from the body
                document.body.removeChild(link);
            },
            error: function(error) {
                // Handle the error response
                console.error("Export failed:", error);
            }
        });
    }

// update trainer 
function submitUpdateTrainer() {
	
	 // Extract the ID from the URL
    var url = window.location.href;
    var id = url.substring(url.lastIndexOf('/') + 1);
    
    // Validate CSRF token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Create FormData object and append form values
    var formData = new FormData();
    formData.append('firstName', document.getElementById('addTrainer-firstname-input').value);
    formData.append('lastName', document.getElementById('addTrainer-lastname-input').value);
    formData.append('cohort', document.getElementById('addTrainer-cohort-input').value);
    formData.append('country', document.getElementById('addTrainer-country-input').value);
    formData.append('language', document.getElementById('addTrainer-language-input').value);
    formData.append('gender', document.getElementById('addTrainer-gender-input').value);
    formData.append('organization', document.getElementById('addTrainer-organization-input').value);
    formData.append('position', document.getElementById('addTrainer-position-input').value);
    formData.append('email', document.getElementById('addTrainer-email-input').value);
    formData.append('telephone', document.getElementById('addTrainer-telephone-input').value);

    // Validate the form fields
    if (!formData.has('firstName') || !formData.has('lastName') || !formData.has('cohort') || !formData.has('country') || !formData.has('language') || !formData.has('gender') || !formData.has('organization') || !formData.has('position') || !formData.has('email') || !formData.has('telephone')) {
        
        showAlert('warning', 'Please fill in all fields');
        return;
    }

    // Validate email format
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.get('email').match(emailRegex)) {
         showAlert('warning', 'Please enter a valid email address');
        return;
    }

    // Validate telephone to be only digits
    var telephoneRegex = /^\d+$/;
    if (!formData.get('telephone').match(telephoneRegex)) {
        showAlert('warning','Telephone should only contain digits');
        return;
    }

    // Make an AJAX request to your controller
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/roster/update/" + id, true);
    xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);

    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                // Handle success response
                showAlert('success', 'Trainer updated successfully!');
            } else {
                // Handle error response
                 showAlert('danger', 'Error updating trainer: ' + xhr.responseText,);
            }
        }
    };

    // Send the form data
    xhr.send(formData);
}


// Add new deployment 
function submitAddDeployment() {
    var trainerId = window.location.pathname.split('/').pop();
    // Validate CSRF token
    var csrfToken = $("meta[name='_csrf']").attr("content");

    if (csrfToken) {
        // Retrieve values from the form
        var role = $("#deployment-role-input").val();
        var status = $("#deployment-status-input").val();
        var type = $("#deployment-type-input").val();
        var expertise = $("#deployment-expertise-input").val();
        var organism = $("#deployment-organism-input").val();
        var startDate = $("#deployment-start-date-input").val();
        var endDate = $("#deployment-end-date-input").val();
        var country = $("#deployment-country-input").val();

        // Validate the form fields
        if (!role || !status || !type || !expertise || !organism || !startDate || !endDate || !country) {
            showAlert('warning', 'Please fill in all fields');
            return;
        }

        // Validate that the start date is not beyond the end date
        if (new Date(startDate) > new Date(endDate)) {
            showAlert('warning', 'Start date cannot be beyond the end date');
            return;
        }

        // Make an AJAX request to your controller
        $.ajax({
            type: "POST",
            url: "/deployments/add/" + trainerId,
            beforeSend: function(xhr) {
                xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
            },
            contentType: "application/json", 
            data: JSON.stringify({
                role: role,
                status: status,
                type: type,
                expertise: expertise,
                deployingOrganism: organism,
                startDate: startDate,
                endDate: endDate,
                country: country
            }),
            success: function(response) {
                showAlert('success', 'Deployment added successfully!');
            },
            error: function(xhr, status, error) {
                showAlert('danger', 'Error adding deployment: ' + xhr.responseText);
            }
        });

    } else {
        showAlert('danger', 'CSRF token not available.');
    }
}


// Manage deployment 

// Function to handle the delete button click
function confirmDeleteDeployment(trainerId) {
    var confirmDelete = confirm("Are you sure you want to delete this deployment?");
    
    if (confirmDelete) {
        // Fetch CSRF token
        var csrfToken = getCsrfToken();

        if (csrfToken) {
            // Make an AJAX request to your delete controller
            var xhr = new XMLHttpRequest();
            xhr.open("POST", "/deployments/delete/" + trainerId, true);
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);

            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        // Handle success response
                        showAlert('Deployment deleted successfully!', 'success');
                    } else {
                        // Handle error response
                        showAlert('Error deleting deployment: ' + xhr.responseText, 'danger');
                    }
                }
            };

            // Send the DELETE request
            xhr.send();
        } else {
            showAlert('CSRF token not available.', 'danger');
        }
    }
}

// Edit deployment 
function submitEditDeployment() {
    var formData = {
		'country': getValue('deployment-country-input'),
        'role': getValue('deployment-role-input'),
        'status': getValue('deployment-status-input'),
        'type': getValue('deployment-type-input'),
        'expertise': getValue('deployment-expertise-input'),
        'deployingOrganism': getValue('deployment-organism-input'),
        'startDate': getValue('deployment-start-date-input'),
        'endDate': getValue('deployment-end-date-input')
    };

    // Check for empty fields
    if (Object.values(formData).some(value => !value)) {
        showAlert('warning','All fields are required. Please fill in all the information.');
        return;
    }

    // Check if end date is before start date
    if (new Date(formData.endDate) < new Date(formData.startDate)) {
        showAlert('warning',"End date must be after the start date.");
        return;
    }

    // CSRF token validation
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");
    if (!csrfToken || !csrfHeader) {
        showAlert('warning',"CSRF token not found. Please refresh the page and try again.");
        return;
    }

    // Get the deploymentId from the URL
    var deploymentId = window.location.pathname.split('/').pop();

    // Submit the form using AJAX
    fetch('/deployments/update/' + deploymentId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(formData)
    })
    .then(response => {
        if (response.ok) {
            showAlert('success', 'Deployment updated successfully');
        } else {
            showAlert('warning', 'Error updating deployment. Please try again.');
        }
    })
    .catch(error => showAlert('warning', 'Error updating deployment. Please try again.'));
}



function getValue(elementId) {
    var element = document.getElementById(elementId);

    // Check if the element exists
    if (element !== null) {
        return element.value;
    } else {
        console.error("Element with ID '" + elementId + "' not found.");
        return null; // Or handle the absence of the element in a way that makes sense for your application
    }
}

// Function to confirm and delete an announcement
function confirmDeleteAnnouncement(announcementId) {
    
    // CSRF token validation
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Check if CSRF token is available
    if (csrfToken) {

        var confirmationMessage = "Are you sure you want to delete this announcement?";

        // Display a confirmation dialog
        var isConfirmed = confirm(confirmationMessage);

        // Check if the user confirmed the deletion
        if (isConfirmed) {
            // If confirmed, proceed with the delete action

            // Create a new XMLHttpRequest object
            var xhr = new XMLHttpRequest();

            // Configure the request
            xhr.open("POST", "/announcement/delete/" + announcementId, true);
            xhr.setRequestHeader(csrfHeader, csrfToken);

            // Set up the onload and onerror callbacks
            xhr.onload = function () {
                if (xhr.status === 200) {
                    // Successful response
                    location.reload();
                } else {
                    // Error response
                    showAlert('error', "Error deleting Announcement with ID " + announcementId);
                }
            };

            xhr.onerror = function () {
                // Network error
                showAlert('warning', "Network error while deleting Announcement with ID " + announcementId);
            };

            // Send the request
            xhr.send();
        } else {
            showAlert('warning', "Deletion of Announcement with ID " + announcementId + " has been canceled.");
        }
    } else {
        // CSRF token not available
        showAlert('warning', "CSRF token not available. Unable to perform delete action.");
    }
}


        
// Function to add a new announcemnt
$(document).ready(function() {
            // Find the form element
            var form = $('#addAnnouncementForm');

            // Add event listener to the form submission event
            form.submit(function(event) {
                // Prevent the default form submission behavior
                event.preventDefault();

                // CSRF token
                var csrfToken = $("meta[name='_csrf']").attr("content");
                var csrfHeader = $("meta[name='_csrf_header']").attr("content");

                // Get the content from the Quill editor instance
                var quillContent = $('#announcementContent').val();

                // Create FormData object and append form data
                var formData = new FormData(form[0]); 
                formData.append('announcementContent', quillContent);

                $.ajax({
                    type: 'POST',
                    url: '/announcement/add',
                    enctype: 'multipart/form-data',
                    beforeSend: function(xhr) {
                        xhr.setRequestHeader(csrfHeader, csrfToken); 
                    },
                    data: formData,
                    processData: false, 
                    contentType: false, 
                    cache: false, 
                    success: function(response) {
                        // Handle success response
                        showAlert('success', 'Announcement added successfully.');
                        console.log(response);
                    },
                    error: function(xhr, status, error) {
                        // Handle error
                        var errorMessage = "Error occurred while adding announcement.";
                        showAlert('error', errorMessage);
                        console.error(xhr.responseText);
                    }
                });
            });

            // Initialize Quill editor
            var quill = new Quill('#email-editor', {
                theme: 'snow' // 'snow' is the default theme
            });
        });
// Function to disable a user
function confirmDisableUser(userId) {
    if (confirm('Are you sure you want to disable this user?')) {
        // Obtain CSRF token from meta tags
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Create data object with user ID
        var data = {
            userId: userId
        };

        // Add CSRF token to headers
        var headers = {};
        headers[csrfHeader] = csrfToken;

        // Send AJAX request to disable user
        $.ajax({
            type: 'POST',
            url: '/users/disable',
            headers: headers,
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                 location.reload();
            },
            error: function(xhr, status, error) {
                // Handle error response
                console.error('Error disabling user:', error);
                showAlert('error', 'Failed to disable user. Please try again later.');
            }
        });
    }
}


// Function to enable a user
function confirmEnableUser(userId) {
    if (confirm('Are you sure you want to enable this user?')) {
        // Obtain CSRF token from meta tags
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Create data object with user ID
        var data = {
            userId: userId
        };

        // Add CSRF token to headers
        var headers = {};
        headers[csrfHeader] = csrfToken;

        // Send AJAX request to enable user
        $.ajax({
            type: 'POST',
            url: '/users/enable',
            headers: headers,
            contentType: 'application/json', // Set the correct Content-Type
            data: JSON.stringify(data),
            success: function(response) {
                location.reload();
            },
            error: function(xhr, status, error) {
                // Handle error response
                console.error('Error enabling user:', error);
                showAlert('error', 'Failed to enable user. Please try again later.');
            }
        });
    }
}



// Function to delete a user
function confirmDeleteUser(email) {
    if (confirm('Are you sure you want to delete this user?')) {
        // Obtain CSRF token from meta tags
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Create data object with email
        var data = {
            email: email
        };

        // Add CSRF token to headers
        var headers = {};
        headers[csrfHeader] = csrfToken;

        // Send AJAX request to delete user
        $.ajax({
            type: 'POST', // Change the request type to DELETE
            url: '/users/delete/' + encodeURIComponent(email), // Pass email as part of the URL
            headers: headers,
            contentType: 'application/json',
            success: function(response) {
                // Handle success response
                showAlert('success', 'User deleted successfully.');
            },
            error: function(xhr, status, error) {
                // Handle error response
                console.error('Error deleting user:', error);
                showAlert('error', 'Failed to delete user. Please try again later.');
            }
        });
    }
}


// Update user profile
function updateUserProfile() {
    // Get form data
    var formData = {
        firstName: $('#addTrainer-firstname-input').val(),
        lastName: $('#addTrainer-lastname-input').val(),
        email: $('#addTrainer-email-input').val()
    };

    // Perform form validation
    if (!formData.firstName || !formData.lastName || !formData.email) {
        showAlert('error', 'All fields are required.');
        return;
    }

    // Validate email format
    var emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(formData.email)) {
        showAlert('error', 'Please enter a valid email address.');
        return;
    }

    // Add CSRF token to headers
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");
    var headers = {};
    headers[csrfHeader] = csrfToken;

    // Send AJAX request
    $.ajax({
        type: 'POST',
        url: '/users/update',
        headers: headers,
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            // Handle success response
            console.log('User updated successfully:', response);
            showAlert('success', 'User updated successfully.');
        },
        error: function(xhr, status, error) {
            // Handle error response
            console.error('Error updating user:', error);
            showAlert('error', 'Failed to update user. Please try again later.');
        }
    });
}


// Function to add a new project 
document.addEventListener('DOMContentLoaded', function() {
    // Add event listener to the form for submit event
    document.getElementById('addProjectForm').addEventListener('submit', function(event) {
        // Prevent default form submission behavior
        event.preventDefault();

        // Retrieve form data
        var projectName = document.getElementById('projectName').value;
        var startDate = document.getElementById('startDate').value;
        var endDate = document.getElementById('endDate').value;
        var workingGroup = document.getElementById('workingGroup').value;

        // Validate form data
        if (!projectName || !startDate || !endDate || !workingGroup) {
            showAlert('warning', 'Please fill in all fields.');
            return;
        }

        // Check if start date is not later than end date
        if (new Date(startDate) > new Date(endDate)) {
            showAlert('warning', 'Start date cannot be later than end date.');
            return;
        }

        // Validate CSRF token
        var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Send AJAX request to add project
        fetch('/projects/add?workingGroupName=' + encodeURIComponent(workingGroup), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                projectName: projectName,
                startDate: startDate,
                endDate: endDate
            })
        })
        .then(response => {
            if (response.ok) {
                showAlert('success', 'Project added successfully!');
                // Optionally, reset the form after successful submission
                document.getElementById('addProjectForm').reset();
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Error adding project');
                });
            }
        })
        .catch(error => {
            showAlert('danger', error.message || 'Error adding project');
        });
    });
});





// Delete project 
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.delete-project-btn').forEach(function(button) {
        button.addEventListener('click', function(event) {
            event.preventDefault(); 

            var projectId = this.getAttribute('data-project-id');
            confirmDeleteProject(projectId);
        });
    });
});

function confirmDeleteProject(projectId) {
    

    var csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    var csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    var confirmation = confirm("Are you sure you want to delete this project?");

    if (confirmation) {
        $.ajax({
            type: "POST",
            url: "/projects/delete/" + projectId,
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function(data) {
                showAlert("success", data);
                window.location.reload();
            },
            error: function(xhr, status, error) {
                showAlert("warning", xhr.responseText);
            }
        });
    }
}




// Membership Application

$(document).ready(function() {
    $('#membershipForm').submit(function(event) {
        event.preventDefault(); 
        
        if (validateForm()) {
            var formData = $('#membershipForm').serializeArray(); // Get form data as array
            
            // Validate CSRF token
            var csrfToken = $("meta[name='_csrf']").attr("content");
            var csrfHeader = $("meta[name='_csrf_header']").attr("content");
            
            $.ajax({
                type: 'POST',
                url: '/applications/add',
                data: formData, // Pass the serialized form data directly
                beforeSend: function(xhr) {
                    // Set CSRF token in request header
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                },
                success: function(response) {
                    // Display success message from the server
                    showAlert('success', response);
                     $("#membershipForm")[0].reset();
                },
                error: function(xhr, status, error) {
                    // Log and display the error response from the server
                    console.error('Form submission failed');
                    console.log(xhr.responseText); 
                    showAlert('error', xhr.responseText); 
                }
            });
        } else {
            console.error('Form validation failed');
            showAlert('warning', 'Form validation failed. Please check your inputs.');
        }
    });
});






function validateForm() {
    var firstName = $('#input-first-name').val().trim();
    var lastName = $('#input-last-name').val().trim();
    var email = $('#input-email').val().trim();
    var nationality = $('#select-nationality').val().trim();
    var sex = $('#select-sex').val().trim();
    var position = $('#input-position').val().trim(); // Added position field
    var organization = $('#select-organization').val().trim();
    var profession = $('#select-profession').val().trim();
    var language = $('#select-language').val().trim();
    var expertise = $('#select-expertise').val().trim();
    var pheocExp = $('input[name="pheocExp"]:checked').val();
    var roster = $('input[name="roster"]:checked').val();
    var statement = $('#textarea-statement').val().trim();

    // Check if any field is empty
    if (
        firstName === '' ||
        lastName === '' ||
        email === '' ||
        nationality === '' ||
        sex === '' ||
        position === '' || 
        organization === '' ||
        profession === '' ||
        language === '' ||
        expertise === '' ||
        !pheocExp ||
        !roster ||
        statement === ''
    ) {
        showAlert('warning', 'Please fill in all the required fields.');
        return false;
    }

    // Validate email format
    var emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        showAlert('warning', 'Please enter a valid email address.');
        return false;
    }

    return true;
}
//Application Management 

document.addEventListener('DOMContentLoaded', function() {
    // Membership Approval
    var applicationId = getApplicationIdFromURL();
    var csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    var csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    // Get the buttons
    var approveButton = document.getElementById("approveButton");
    var rejectButton = document.getElementById("rejectButton");

    // Add event listeners to the buttons
    approveButton.addEventListener("click", function() {
        approveMembership();
    });

    rejectButton.addEventListener("click", function() {
        rejectMembership();
    });

    // Function to handle approving membership
    function approveMembership() {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/applications/approve/" + applicationId, true);
        xhr.setRequestHeader(csrfHeader, csrfToken);
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    showAlert('success', xhr.responseText);
                } else {
                    showAlert('warning', xhr.responseText);
                }
            }
        };
        xhr.send();
    }

    // Function to handle rejecting membership
    function rejectMembership() {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/applications/reject/" + applicationId, true);
        xhr.setRequestHeader(csrfHeader, csrfToken);
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    showAlert('success', xhr.responseText);
                } else {
                    showAlert('warning', xhr.responseText);
                }
            }
        };
        xhr.send();
    }
	
    // Function to extract application ID from URL
    function getApplicationIdFromURL() {
        var url = window.location.href;
        var index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }
   
});



    // Function to display alerts
    function showAlert(type, message) {
        var alertBox = document.getElementById('alertBox');
        var alertMessage = alertBox.querySelector('.alert-message');
        var alertIcon = alertBox.querySelector('.uil');

        alertMessage.textContent = message;

        if (type === 'success') {
            alertBox.classList.remove('alert-warning');
            alertBox.classList.add('alert-success');
        } else {
            alertBox.classList.remove('alert-success');
            alertBox.classList.add('alert-warning');
        }

        alertBox.style.display = 'block';

        setTimeout(function() {
            alertBox.style.display = 'none';
        }, 10000); 
    }


// Forum - post new topic

function postTopic() {
    // Validate CSRF token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Get topic data from the form
    var topicTitle = $("#topicTitle").val().trim();
    var topicContent = $("#topicContent").val().trim();
    var attachmentFile = $("#attachmentFile");

    // Check if title and content are not empty
    if (topicTitle === '' || topicContent === '') {
        showAlert('danger', 'Title and content cannot be empty');
        return;
    }

    // Extract category name from the URL dynamically
    var categoryName = window.location.pathname.split('/').pop();

    // Create FormData object to send data with files
    var formData = new FormData();
    formData.append("categoryName", categoryName); 
    formData.append("title", topicTitle); 
    formData.append("content", topicContent); 
    formData.append("attachment", attachmentFile.prop('files')[0]);

    // Send Ajax request
    $.ajax({
        url: "/api/topic/create", 
        type: "POST",
        data: formData,
        beforeSend: function(xhr) {
            // Set CSRF token in the request header with the appropriate header name
            xhr.setRequestHeader('X-CSRF-Token', csrfToken); 
        },
        contentType: false,
        processData: false,
        success: function(response) {
            // Display success message in the alert box
            showAlert('success', 'Topic posted successfully!');

            // Clear the textboxes
            $("#topicTitle").val('');
            $("#topicContent").val('');
            attachmentFile.val(''); 
        },
        error: function(xhr, status, error) {
            // Display error message in the alert box
            showAlert('error', 'Failed to post topic: ' + xhr.responseText);
        }
    });
}

// Like a post
$(document).ready(function() {
    // Function to handle the AJAX request when the like button is clicked
    $('form.like-form').submit(function(event) {
        event.preventDefault(); // Prevent default form submission

        // Get the post ID from the hidden input field
        var postId = $(this).find('input[name="postId"]').val();

        // Validate CSRF token
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Send the AJAX request
        $.ajax({
            type: 'POST',
            url: '/api/likes/like/' + postId,
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'X-CSRF-HEADER': csrfHeader
            },
            success: function(response) {
                // Check if the response indicates the user has already liked the topic
                if (response.message === "You have already liked this topic") {
                    alert(response.message); 
                } else {
                    // Update the like count on the page
                    $('#like-count').text(response.likes);
                }
            },
            error: function(xhr, status, error) {
                console.error(xhr.responseText);
            }
        });
    });
});




// Comment 

$(document).ready(function() {
    // Function to handle the AJAX request when the comment button is clicked
    $('#comment-button').click(function() {
        // Get the content of the comment from the textarea
        var commentContent = $('#floatingTextarea2').val().trim();
        
        // Check if the comment content is empty
        if (commentContent === '') {
            alert('Comment should not be empty.');
            return; // Abort the AJAX call if the comment is empty
        }
        
        // Extract the topic ID from the current URL
        var url = window.location.href;
        var topicId = url.substring(url.lastIndexOf('/') + 1);
        console.log("Topic ID:", topicId);

        // Validate CSRF token
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Prepare the form data to send in the AJAX request
        var formData = new FormData();
        formData.append('topicId', topicId);
        formData.append('content', commentContent);

        // Check if an attachment is provided
        if ($('#formFileSm')[0].files.length > 0) {
            // Append attachment to the form data
            formData.append('attachment', $('#formFileSm')[0].files[0]);
        }

        // Send the AJAX request to add a new comment
        $.ajax({
            type: 'POST',
            url: '/api/comments/add',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'X-CSRF-HEADER': csrfHeader
            },
            data: formData,
            processData: false,  // Prevent jQuery from automatically processing the data
            contentType: false,  // Set content type to false to prevent jQuery from adding contentType
            success: function(response) {
             // Construct the HTML for the new comment
                var newCommentHtml = '<li class="comment-item">';
                newCommentHtml += '<div class="comment-meta">';
                newCommentHtml += '<div class="user-initials">';
                newCommentHtml += '<span>' + response.initials + '</span>';
                newCommentHtml += '</div>';
                newCommentHtml += '<div class="user-info">';
                newCommentHtml += '<span class="user-name">' + response.userName + '</span>';
                newCommentHtml += '<i class="far fa-calendar-alt"></i>';
                newCommentHtml += '<span class="comment-timestamp">' + response.createdAt + '</span>';
                newCommentHtml += '</div>';
                newCommentHtml += '</div>';
                newCommentHtml += '<div class="comment-content">';
                newCommentHtml += '<p>' + response.content + '</p>';
                // Check if attachment is available
                if (response.attachment) {
                    newCommentHtml += '<div class="attachment">';
                    newCommentHtml += '<p>Attachment:</p>';
                    newCommentHtml += '<a href="/api/comment/attachment/' + response.id + '">' + response.attachmentName + '</a>';
                    newCommentHtml += '</div>';
                }
                newCommentHtml += '<p>You posted this comment. <a href="#">Edit Comment</a></p>';
                newCommentHtml += '</div>';
                newCommentHtml += '</li>';

                // Append the new comment to the comments list
                $('.comments-section ul').append(newCommentHtml);

                // Clear the content of the textarea after adding the comment
                $('#floatingTextarea2').val('');

            },
            error: function(xhr, status, error) {
                console.error(xhr.responseText);
                // Handle error response
                alert('Failed to add comment. Please try again.');
            }
        });
    });
});

//Report Abuse

$(document).ready(function() {
    $('#reportAbuseForm').submit(function(event) {
        // Prevent default form submission
        event.preventDefault();

        // Submit the form via AJAX
        $.ajax({
            type: 'POST',
            url: $(this).attr('action'),
            data: $(this).serialize(),
            success: function(response) {
                // Handle success response
                showAlert('Abuse reported successfully', 'success');
            },
            error: function(xhr, status, error) {
               
                 showAlert('Error: ' + error, 'warning');
                
            }
        });
    });
});

// Edit comment 
$(document).ready(function() {
    $('.edit-comment').click(function(e) {
        e.preventDefault();
        var editUrl = $(this).attr('href');
        window.location.href = editUrl;
    });
});

$(document).ready(function() {
    $('#editCommentForm').submit(function(e) {
        e.preventDefault(); // Prevent the default form submission

        // Get the form data
        var formData = new FormData(this);
        
        // Check if the content is not empty
        var content = formData.get('content');
        if (!content.trim()) {
            alert('Content cannot be empty');
            return;
        }

        // Get the CSRF token
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Set the CSRF token in the request headers
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        });

        // Make the Ajax call to the controller
        $.ajax({
            url: $(this).attr('action'), // Get the action URL from the form
            type: $(this).attr('method'), // Get the method (POST) from the form
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                showAlert('success', 'Comment edited successfully');
                console.log('Comment updated successfully');
            },
            error: function(xhr, status, error) {
                showAlert('error', 'Error editing comment: ' + error);
                console.error('Error updating comment:', error);
            }
        });
    });
});

// Edit topic
$(document).ready(function() {
    $('.edit-topic').click(function(e) {
        e.preventDefault();
        var editUrl = $(this).attr('href');
        window.location.href = editUrl;
    });
});

$(document).ready(function() {
    $('#editTopicForm').submit(function(e) {
        e.preventDefault(); // Prevent the default form submission

        // Get the form data
        var formData = new FormData(this);
        
        // Check if the content is not empty
        var content = formData.get('content');
        if (!content.trim()) {
            alert('Content cannot be empty');
            return;
        }

        // Get the CSRF token
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Set the CSRF token in the request headers
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        });

        // Make the Ajax call to the controller
        $.ajax({
            url: $(this).attr('action'), // Get the action URL from the form
            type: $(this).attr('method'), // Get the method (POST) from the form
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                showAlert('success', 'Topic edited successfully');
            },
            error: function(xhr, status, error) {
                showAlert('error', 'Error editing comment: ' + error);
            }
        });
    });
});

// Forum Management 
$(document).ready(function() {
    // Handle form submissions
    $('#withdrawForm, #publishForm, #deleteForm').submit(function(event) {
        var form = $(this); // Get the form
        var actionType = form.attr('id').replace('Form', ''); // Determine action type (withdraw, publish, delete)
        var message = ''; // Initialize the message

        // Set the message based on the action type
        switch (actionType) {
            case 'withdraw':
                message = 'Withdrawing topic...';
                break;
            case 'publish':
                message = 'Publishing topic...';
                break;
            case 'delete':
                // Confirm before deleting
                if (!confirm('Are you sure you want to delete this topic?')) {
                    event.preventDefault(); 
                    return;
                }
                message = 'Deleting topic...';
                break;
            default:
                message = '';
        }

        // Show the alert
        showAlert('info', message);

        // Prevent default form submission
        event.preventDefault();

        // Perform form submission
        $.ajax({
            type: form.attr('method'),
            url: form.attr('action'),
            data: form.serialize(),
            success: function(response) {
                // Handle success response if needed
                showAlert('success', 'Operation successful');
            },
            error: function(xhr, status, error) {
                // Handle error response if needed
                showAlert('error', 'Operation failed');
            }
        });
    });
});


  function confirmDelete() {
        if (confirm("Are you sure you want to delete this comment?")) {
            var form = $('#deleteForm'); // Select the form
            // Perform form submission using AJAX
            $.ajax({
                type: form.attr('method'),
                url: form.attr('action'),
                data: form.serialize(),
                success: function(response) {
                    // Handle success response if needed
                    showAlert('success', 'Comment deleted successfully');
                },
                error: function(xhr, status, error) {
                    // Handle error response if needed
                    showAlert('error', 'Failed to delete comment');
                }
            });
        } else {
            // Display a cancel message in the alert box
            showAlert('info', 'Deletion canceled');
        }
    }
$(document).ready(function() {
    // Add CSRF token to AJAX requests
    $.ajaxSetup({
        headers: {
            'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
        }
    });

    // Handle join button click
    $('.join-button').click(function() {
        var formId = $(this).data('form-id');
        var groupId = $(this).closest('form').find('input[name="working_group_id"]').val(); // Get the working group ID from the hidden input field

        // Display a confirmation dialog before joining the group
        if (confirm('Are you sure you want to join this group?')) {
            console.log("Form ID:", formId); // Debugging
            console.log("Group ID:", groupId); // Debugging

            // Submit the form using AJAX
            $.ajax({
                type: 'POST',
                url: $('#' + formId).attr('action'),
                data: { working_group_id: groupId }, // Send the working group ID
                success: function(response) {
                    // Handle successful join
                    showAlert('success', response); 
                },
                error: function(xhr, status, error) {
                    // Handle errors
                    console.error(xhr.responseText);
                    showAlert('error', xhr.responseText); 
                }
            });
        }
    });
});

// Working group functions - cancel request and leave

$(document).ready(function() {
    // Handle leave group form submission
    $('.leave-form').submit(function(event) {
        // Prevent the default form submission behavior
        event.preventDefault();

        // Extract form data
        var form = $(this);
        var requestId = form.find('input[name="requestId"]').val();

        // Get the CSRF token and header
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Prepare data for AJAX request
        var requestData = {
            requestId: requestId,
        };

        // Add CSRF token to data
        requestData[csrfHeader] = csrfToken;

        // Send AJAX request
        $.ajax({
            type: 'POST',
            url: form.attr('action'),
            data: requestData,
            success: function(response) {
                // Handle success response
                showAlert('success', response);
            },
            error: function(xhr, status, error) {
                // Handle error response
                showAlert('error', xhr.responseText);
            }
        });
    });

    // Handle cancel request form submission
    $('.cancel-form').submit(function(event) {
        // Prevent the default form submission behavior
        event.preventDefault();

        // Extract form data and CSRF token
        var form = $(this);
        var requestId = form.find('input[name="requestId"]').val();
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Prepare data for AJAX request
        var requestData = {
            requestId: requestId,
        };

        // Add CSRF token to data
        requestData[csrfHeader] = csrfToken;

        // Send AJAX request
        $.ajax({
            type: 'POST',
            url: form.attr('action'),
            data: requestData,
            success: function(response) {
                // Handle success response
                showAlert('success', response);
            },
            error: function(xhr, status, error) {
                // Handle error response
                showAlert('error', xhr.responseText);
            }
        });
    });

  
});

// Update user's profile 

function previewProfilePicture(event) {
    var selectedFile = event.target.files[0]; // Get the selected file

    var imagePreview = document.getElementById('imagePreview');
    imagePreview.style.display = 'none'; // Hide the image preview initially
    imagePreview.src = ''; // Reset the src attribute

    var existingProfilePicture = document.getElementById('existingProfilePicture');
    if (existingProfilePicture) {
        existingProfilePicture.style.display = 'none'; // Hide the existing profile picture
    }

    var defaultInitials = document.querySelector('.user-initials');
    if (defaultInitials) {
        defaultInitials.style.display = 'none'; // Hide the default initials
    }

    if (selectedFile) {
        var reader = new FileReader(); // Create a FileReader object

        reader.onload = function () { // Set up the onload function
            imagePreview.src = reader.result; // Update the src attribute with the data URL
            imagePreview.style.display = 'inline'; // Display the image preview
        };

        reader.readAsDataURL(selectedFile); // Read the selected file as a data URL
    }
}


 $(document).ready(function() {
        function saveChanges() {
   		// Get the CSRF token and header
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Gather data from input fields
    var firstName = document.getElementById('inputFirstName').value.trim();
    var lastName = document.getElementById('inputLastName').value.trim();
    var gender = document.getElementById('inputGender').value.trim(); 
    var nationality = document.getElementById('inputNationality').value.trim();
    var position = document.getElementById('inputPosition').value.trim();
    var affiliation = document.getElementById('inputAffiliation').value.trim();
    var expertise = document.getElementById('inputExpertise').value.trim();
    var profession = document.getElementById('inputProfession').value.trim();
    var bio = document.getElementById('inputBio') ? document.getElementById('inputBio').value.trim() : null;


    // Get the current profile picture if it exists
    	var currentProfilePictureElement = document.querySelector('.profile-img');
		var currentProfilePicture;
		if (currentProfilePictureElement) {
		    currentProfilePicture = currentProfilePictureElement.getAttribute('src');
		} else {
		    // Handle the case when the element doesn't exist
		    console.log('No profile picture element found.');
		}

    // Check if a new profile picture has been uploaded
    var newProfilePicture = document.querySelector('input[name="photo"]').files[0];

    // Perform client-side validation
    if (!firstName || !lastName || !gender || !nationality || !position || !affiliation || !expertise || !profession) {
        showAlert('warning', 'Please fill out all required fields.');
        return;
    }

    // Prepare data to send to the server
    var userData = {
        firstName: firstName,
        lastName: lastName,
        gender: gender,
        nationality: nationality,
        position: position,
        organization: affiliation,
        expertise: expertise,
        profession: profession,
        bio: bio ,
       
    };

    // Create FormData object to send profile picture if it's changed
    var formData = new FormData();
    formData.append('userData', JSON.stringify(userData)); // Convert user data to JSON string

    // If a new profile picture is uploaded, append it to FormData
    if (newProfilePicture) {
        formData.append('photo', newProfilePicture, newProfilePicture.name);
    }

    // Make an AJAX request to send the data to the server
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/members/update-profile', true);
    xhr.setRequestHeader(csrfHeader, csrfToken);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                // Handle successful response from the server
                showAlert('success', xhr.responseText);
            } else {
                // Handle error response from the server
                showAlert('danger', xhr.responseText);
            }
        }
    };

    xhr.send(formData); // Send FormData object containing user data and profile picture
}

        // Event handler for the saveProfile button
        $('#saveProfile').on('click', function(event) {
            event.preventDefault(); // Prevent the default form submission behavior

            // Call the saveChanges() function when the button is clicked
            saveChanges();
        });
    });



// change password 
function validatePassword() {
    var currentPassword = document.getElementById("inputPasswordCurrent").value;
    var newPassword = document.getElementById("inputPasswordNew").value;
    var confirmPassword = document.getElementById("inputPasswordNew2").value;

    if (currentPassword === "" || newPassword === "" || confirmPassword === "") {
        showAlert("danger", "Please fill in all the fields.");
    } else if (newPassword !== confirmPassword) {
        showAlert("danger", "New password and verify password do not match.");
    } else if (newPassword.length < 8) {
        showAlert("danger", "Password must be at least 8 characters long.");
    } else if (!containsUpperCase(newPassword)) {
        showAlert("danger", "Password must contain at least one uppercase letter.");
    } else if (!containsLowerCase(newPassword)) {
        showAlert("danger", "Password must contain at least one lowercase letter.");
    } else if (!containsNumber(newPassword)) {
        showAlert("danger", "Password must contain at least one number.");
    } else if (!containsSpecialCharacter(newPassword)) {
        showAlert("danger", "Password must contain at least one special character.");
    } else {
        // Prepare the form data to be sent to the server
        var formData = new FormData();
        formData.append("oldPassword", currentPassword);
        formData.append("newPassword", newPassword);
        formData.append("confirmPassword", confirmPassword);

        // Validate CSRF token
        var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Send the form data to the server using AJAX
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/users/change-password", true);
        xhr.setRequestHeader(csrfHeader, csrfToken);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    // Password updated successfully
                    showAlert("success", xhr.responseText);
                } else {
                    // Error updating password
                    showAlert("danger", xhr.responseText);
                }
            }
        };

        // Serialize the form data
        var serializedData = [];
        for (var pair of formData.entries()) {
            serializedData.push(encodeURIComponent(pair[0]) + '=' + encodeURIComponent(pair[1]));
        }
        serializedData = serializedData.join('&');

        xhr.send(serializedData);
    }
}

// Functions for password validation

function containsUpperCase(str) {
    return /[A-Z]/.test(str);
}

function containsLowerCase(str) {
    return /[a-z]/.test(str);
}

function containsNumber(str) {
    return /[0-9]/.test(str);
}

function containsSpecialCharacter(str) {
    return /[!@#$%^&*(),.?":{}|<>]/.test(str);
}



// changes Notification preferences 

function savePrefs() {
    // Retrieve the CSRF token and header
    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Retrieve the checkbox element and its value
    let isEnableCheckbox = document.getElementById("switch1");
    let isEnable = isEnableCheckbox.value; 

    // Prepare the form data
    var formData = new FormData();
    formData.append("isEnable", isEnable); 

    // Log the checkbox value and form data
    console.log("Checkbox Value:", isEnable);
    console.log("Form Data:", formData);

    // Send the AJAX request
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/notifications/update", true);
    xhr.setRequestHeader(csrfHeader, csrfToken);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                showAlert("success", xhr.responseText);
            } else {
                showAlert("danger", xhr.responseText);
            }
        }
    };

    // Send the form data in the request
    xhr.send(formData);
}




// Function to download the CV file
function downloadCV() {
    // Trigger the AJAX request to download the CV file
    fetch('/roster/downloadCV', {
        method: 'GET'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to download CV file');
        }
        return response.blob();
    })
    .then(blob => {
        // Create a URL for the blob
        var url = window.URL.createObjectURL(blob);
        // Create a temporary anchor element to trigger the download
        var a = document.createElement('a');
        a.href = url;
        
        // Provide a default filename for the downloaded file
        var filename = 'document_' + new Date().getTime() + '.pdf'; // Example: document_timestamp.pdf
        a.download = filename.trim(); 
        
        // Append the anchor to the body and trigger the download
        document.body.appendChild(a);
        a.click();
        // Remove the temporary anchor
        document.body.removeChild(a);
    })
    .catch(error => {
        console.error('Error downloading CV file:', error);
        showAlert('danger', 'Failed to download CV file. Please try again later.');
    });
}



// Update project details 
function submitUpdateProject(event) {
    // Prevent default form submission behavior
    event.preventDefault();

    var projectId = document.getElementById('ProjectIdInput').value;
    var projectName = document.getElementById('editProject-name-input').value;
    var status = document.getElementById('editProject-status-input').value;
    var startDate = document.getElementById('editProject-startDate-input').value;
    var endDate = document.getElementById('editProject-endDate-input').value;

    // Validate form data
    if (!projectName || !status || !startDate || !endDate) {
        showAlert('warning', 'Please fill in all fields.');
        return;
    }

    // Check if start date is not later than end date
    if (new Date(startDate) > new Date(endDate)) {
        showAlert('warning', 'Start date cannot be later than end date.');
        return;
    }

    // Prepare JSON data for AJAX request
    var jsonData = {
        projectId: projectId,
        projectName: projectName,
        status: status,
        startDate: startDate,
        endDate: endDate
    };

    // Send AJAX request to update project details
    fetch('/projects/update/' + projectId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
        },
        body: JSON.stringify(jsonData)
    })
    .then(response => {
        if (response.ok) {
            showAlert('success', 'Project updated successfully!');
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Error updating project');
            });
        }
    })
    .catch(error => {
        showAlert('danger', error.message || 'Error updating project');
    });
}


// Add new task
function submitAddTask() {
    // Retrieve values from form inputs
    var task = document.getElementById('task-name-input').value;
    var priority = document.getElementById('task-priority-input').value;
    var responsible = document.getElementById('task-responsible-input').value;
    var startDate = document.getElementById('task-start-date-input').value;
    var targetDate = document.getElementById('task-end-date-input').value;
    var status = document.getElementById('task-status-input').value;
    
    // Retrieve project ID and working group ID
    var projectId = document.getElementById('projectId').value;
    var workingGroupId = document.getElementById('workingGroupId').value;
   

    // Retrieve CSRF token and header
    var csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    var csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
    
    // Perform data validation
    if (!task || !priority || !responsible || !startDate || !targetDate || !status ) {
        showAlert('warning', 'Please fill in all fields.');
        return;
    }

    // Check if start date is not later than end date
    if (new Date(startDate) > new Date(targetDate)) {
        showAlert('warning', 'Start date cannot be later than end date.');
        return;
    }

    // Prepare JSON data
    var jsonData = {
        task: task,
        priority: priority,
        responsible: responsible,
        startDate: startDate,
        targetDate: targetDate,
        status: status,
        projectId: projectId,
        workingGroupId: workingGroupId 
    };

console.log('JSON Data:', jsonData);
    // Send AJAX request
    fetch('/tasks/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(jsonData)
    })
    .then(response => {
        if (response.ok) {
            showAlert('success', 'Task added successfully!');
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Error adding task');
            });
        }
    })
    .catch(error => {
        alert(error.message);
    });
}

// Delete a task

function confirmDeleteTask(taskId) {
    // Prompt the user for confirmation
    var confirmation = confirm("Are you sure you want to delete this task?");
    
    if (confirmation) {
        // Validate CSRF token
        var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        // Make an AJAX request to delete the task
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/tasks/delete/" + taskId, true);
        xhr.setRequestHeader(csrfHeader, csrfToken); // Set CSRF token in the header
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                // If deletion is successful, refresh the page
                location.reload();
            } else {
                // If deletion fails, display an alert with the error message
                alert("Failed to delete task. Error: " + xhr.responseText);
            }
        };
        
        xhr.onerror = function() {
            // If there's a network error, display an alert
            alert("Network Error. Please try again later.");
        };
        
        xhr.send();
    }
}




// Update a task
function updateTask() {
    var taskId = document.getElementById('taskIdInput').value;
    var task = document.getElementById('taskNameInput').value;
    var startDate = document.getElementById('startDateInput').value;
    var targetDate = document.getElementById('endDateInput').value;
    var priority = document.getElementById('prioritySelect').value;
    var responsible = document.getElementById('responsibleInput').value;
    var status = document.getElementById('statusSelect').value;

    // Check if all fields are filled
    if (!task || !startDate || !targetDate || !priority || !responsible || !status) {
        showAlert('warning', 'Please fill in all fields.');
        return;
    }

    // Check if start date is not later than end date
    if (new Date(startDate) > new Date(targetDate)) {
        showAlert('warning', 'Start date cannot be later than end date.');
        return;
    }

    // Validate CSRF token
    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Prepare data for AJAX request
    var formData = {
        taskId: taskId,
        task: task,
        startDate: startDate,
        targetDate: targetDate,
        priority: priority,
        responsible: responsible,
        status: status
    };

    // Send AJAX request to update task
    fetch('/tasks/update/' + taskId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken 
        },
        body: JSON.stringify(formData)
    })
    .then(response => {
        if (response.ok) {
            showAlert('success', 'Task updated successfully!');
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Error updating task');
            });
        }
    })
    .catch(error => {
        showAlert('danger', error.message || 'Error updating task');
    });
}

document.addEventListener("DOMContentLoaded", function() {
    // Update trainer's status
    $('#updateStatusForm').submit(function(event) {
        event.preventDefault(); // Prevent default form submission behavior
        
        var formData = $(this).serialize(); // Serialize form data
            // Validate CSRF token
		    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
		    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Send AJAX request to update status
        $.ajax({
            type: 'POST',
            url: '/roster/updateStatus',
            data: formData,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken); // Set CSRF token in header
            },
            success: function(response) {
                showAlert('success', response); // Show success message
                // Display success message in the alert box for updating status
                $('#statusAlertBox').removeClass('alert-warning').addClass('alert-success');
                $('#statusAlertBox .alert-message').text('Status updated successfully.');
                $('#statusAlertBox').show(); // Display the alert box
            },
            error: function(xhr, status, error) {
                showAlert('error', xhr.responseText); // Show error message
            }
        });
    });

    // Upload resume
    document.getElementById("uploadResumeForm").addEventListener("submit", function (event) {
        event.preventDefault(); // Prevent default form submission

        // Validate form fields
        var resumeFile = document.getElementById("resumeFile").files[0];
        if (!resumeFile) {
            showAlert("danger", "Please select a resume file.");
            return;
        }

           // Validate CSRF token
		    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
		    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Prepare form data
        var formData = new FormData();
        formData.append("resumeFile", resumeFile);

        // Send AJAX request
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "roster/uploadResume", true);
        xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    // Handle successful response
                    showAlert("success", "Resume uploaded successfully.");
                    // Display success message in the alert box for uploading CV
                    $('#cvAlertBox').removeClass('alert-warning').addClass('alert-success');
                    $('#cvAlertBox .alert-message').text('CV uploaded successfully.');
                    $('#cvAlertBox').show(); // Display the alert box
                } else {
                    // Handle error response
                    showAlert("danger", "Failed to upload resume.");
                }
            }
        };
        xhr.send(formData);
    });
});

// In-country training

function submitAddTraineeForm() {
    // Fetch the CSRF token
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    
    // Get reference to the form
    const form = document.getElementById('addTraineeForm');

    // Retrieve form inputs
    const firstName = form.elements['firstName'].value.trim();
    const lastName = form.elements['lastName'].value.trim();
    const email = form.elements['email'].value.trim();
    const phoneNumber = form.elements['phoneNumber'].value.trim();
    const organization = form.elements['organization'].value.trim();
    const position = form.elements['position'].value.trim();
    const moduleAttended = form.elements['moduleAttended'].value.trim();
    const trainingDate = form.elements['trainingDate'].value;
    const country = form.elements['country'].value.trim();
    const role = form.elements['role'].value.trim();
    

    // Basic form validation
    if (firstName === '' || lastName === '' || email === '' || phoneNumber === '' || organization === '' || position === '' || moduleAttended === '' || trainingDate === '' || country === '' || role === '') {
        showAlert('danger', 'Please fill in all fields.');
        return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('danger', 'Please enter a valid email address.');
        return;
    }

    // Create a new FormData object and append form data to it
    const formData = new FormData(form);

    // Convert the FormData object to a serialized string
    const serializedData = new URLSearchParams(formData).toString();

    // Make AJAX request
    fetch('/trainees/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': token
        },
        body: serializedData
    })
    .then(response => {
        if (response.ok) {
            showAlert('success', 'Trainee added successfully!');
            $('#addTraineeForm')[0].reset();
            
        } else {
            showAlert('danger', 'Failed to add trainee. Please try again.');
        }
    })
    .catch(error => {
        console.error('Error adding trainee:', error);
        showAlert('danger', 'An error occurred. Please try again later.');
    });


// Delete trainee

// Get the CSRF token from the meta tag
var csrfToken = $("meta[name='_csrf']").attr("content");

$('.delete-trainee').on('click', function(e) {
    e.preventDefault();
    var traineeId = $(this).data('trainee-id');
    if (confirm('Are you sure you want to delete this trainee?')) {
        $.ajax({
            type: 'DELETE',
            url: '/trainees/delete/' + traineeId,
            beforeSend: function(xhr) {
                // Set the CSRF token in the request header
                xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
            },
            success: function(response) {
                
                location.reload(); 
            },
            error: function(xhr, status, error) {
                console.error(error);
                alert('An error occurred while deleting the trainee.');
            }
        });
    }
});

// Update trainee

function updateTrainee() {
    // Perform form validation
    if ($('#firstName').val() === '' || $('#lastName').val() === '' || $('#email').val() === '' || $('#phoneNumber').val() === '' || $('#organization').val() === '' || $('#position').val() === '' || $('#moduleAttended').val() === '' || $('#trainingDate').val() === '' || $('#country').val() === '' || $('#role').val() === '') {
        showAlert('error', 'Please fill in all the fields.');
        return;
    }

    // Serialize the form data
    var formData = $('#editTraineeForm').serialize();

    // Validate CSRF token
    var csrfToken = $("meta[name='_csrf']").attr("content");
    var csrfHeader = $("meta[name='_csrf_header']").attr("content");

    // Add CSRF token to the serialized form data
    formData += '&' + csrfHeader + '=' + csrfToken;

    // Get the trainee ID from the hidden input field
    var traineeId = $('#trainee-id').val();
    
    console.log('traineeId:', traineeId);

    // Make an AJAX call to the controller
    $.ajax({
        type: 'POST',
        url: '/trainees/update/' + traineeId,
        data: formData,
        success: function(response) {
            // Handle success response
            showAlert('success', 'Trainee updated successfully.');
              $('#editTraineeForm')[0].reset();
        },
        error: function(xhr, status, error) {
            // Handle error response
            showAlert('error', 'Error updating trainee: ' + xhr.responseText);
        }
    });
}


$(document).ready(function() {
    $('#traineeTable').DataTable();

    // Handle filtering when selecting options in dropdowns
    $('#countryFilter, #moduleFilter, #roleFilter').on('change', function() {
        var country = $('#countryFilter').val();
        var module = $('#moduleFilter').val();
        var role = $('#roleFilter').val();

        // Perform filtering based on selected options
        $('#traineeTable').DataTable().search('').draw();
        $('#traineeTable').DataTable().column(8).search(country).draw(); // Filter by country
        $('#traineeTable').DataTable().column(6).search(module).draw(); // Filter by module
        $('#traineeTable').DataTable().column(9).search(role).draw(); // Filter by role
    });
});
}


// Update Announcemnet 

document.addEventListener('DOMContentLoaded', function () {
    // Get the form element
    var updateAnnouncementForm = document.getElementById('editAnnouncementForm');

    // Add event listener for form submission
    updateAnnouncementForm.addEventListener('submit', function (event) {
        // Prevent the default form submission
        event.preventDefault();

        // Validate if all required fields are filled
        var announcementTitle = document.getElementById('announcementTitle').value.trim();
        var announcementContent = document.getElementById('announcementContent').value.trim();

        if (!announcementTitle || !announcementContent) {
            showAlert('warning', 'Please fill in all required fields.');
            return;
        }

        // CSRF token validation
        var csrfToken = $("meta[name='_csrf']").attr("content");
        var csrfHeader = $("meta[name='_csrf_header']").attr("content");

        // Check if CSRF token is available
        if (!csrfToken || !csrfHeader) {
            showAlert('warning', 'CSRF token not available. Unable to perform the update action.');
            return;
        }

        // Create a FormData object to store form data
        var formData = new FormData(updateAnnouncementForm);

        // Create a new XMLHttpRequest object
        var xhr = new XMLHttpRequest();

        // Configure the request
        xhr.open('POST', updateAnnouncementForm.getAttribute('action'), true);
        xhr.setRequestHeader(csrfHeader, csrfToken);

        // Set up the onload and onerror callbacks
        xhr.onload = function () {
            if (xhr.status === 200) {
                // Successful response
                showAlert('success', 'Announcement updated successfully.');
                // Optionally, you can redirect the user to another page after successful update
            } else {
                // Error response
                showAlert('warning', 'Error updating Announcement. Please try again later.');
            }
        };

        xhr.onerror = function () {
            // Network error
            showAlert('warning', 'Network error while updating Announcement.');
        };

        // Send the request with form data
        xhr.send(formData);
    });
});

// Function to reset password 

  document.getElementById("resetPasswordForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent default form submission

    var newPassword = document.getElementById("input-newpassword").value;
    var confirmPassword = document.getElementById("input-confirmpassword").value;

    // Check if passwords match
    if (newPassword !== confirmPassword) {
        alert("Passwords do not match");
        return;
    }

    // Check password strength
    if (!isPasswordStrong(newPassword)) {
        alert("Password must be at least 8 characters long and contain at least one capital letter");
        return;
    }

    // Get the token from the URL
    var urlParams = new URLSearchParams(window.location.search);
    var token = urlParams.get('token');

    // CSRF token retrieval 
    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

    // AJAX request to reset password
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/users/reset-password?token=" + token, true); // Include token in the URL
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken); // Set CSRF token in header
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                // Password reset successful
                alert("Password reset successful!");
                // Redirect or perform further actions if necessary
            } else {
                // Password reset failed
                alert("Password reset failed. Please try again later.");
            }
        }
    };
    xhr.send(JSON.stringify({ newPassword: newPassword }));
});

// Function to validate password strength
function isPasswordStrong(password) {
    // Implement your password strength validation logic here
    // For example, you can check for minimum length, capital letters, etc.
    return password.length >= 8 && /[A-Z]/.test(password);
}


// User request - Reset password 

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('resetPwdButton').addEventListener('click', function () {
        var email = document.getElementById('input-email').value;
        var csrfTokenMetaTag = document.querySelector('meta[name="_csrf"]');
        
       
        var csrfToken = csrfTokenMetaTag.getAttribute('content');

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/users/password-reset', true);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);

        xhr.onload = function () {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                if (response.success) {
                    showAlert('success', response.message);
                    clearEmailInput();
                } else {
                    showAlert('error', response.message);
                }
            } else {
                showAlert('error', 'Error: ' + xhr.status);
            }
        };

        xhr.onerror = function () {
            showAlert('error', 'Request failed');
        };

        xhr.send(JSON.stringify({ email: email }));
    });
});

function clearEmailInput() {
    document.getElementById('input-email').value = '';
}

// Function to handle MFA 

// Function to handle form submission
function submitVerificationCode(event) {
    event.preventDefault(); // Prevent normal form submission

    // Get the entered digits from the input fields
    var digit1 = document.getElementById('digit1-input').value.trim();
    var digit2 = document.getElementById('digit2-input').value.trim();
    var digit3 = document.getElementById('digit3-input').value.trim();
    var digit4 = document.getElementById('digit4-input').value.trim();

    // Concatenate the digits to form the code
    var verificationCode = digit1 + digit2 + digit3 + digit4;

    // Validate the code
    if (!validateDigits(verificationCode)) {
        showAlert('error', 'Please enter a 4-digit code consisting only of digits.');
        return;
    }

    // Retrieve the CSRF token from the page
    var csrfTokenMetaTag = document.querySelector('meta[name="_csrf"]');
    var csrfToken = csrfTokenMetaTag.getAttribute('content');

    // Create an Ajax request
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/verify-code', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken); // Attach CSRF token to header

    // Handle response from server
    xhr.onload = function () {
        if (xhr.status === 200) {
            // Handle success response
            var response = JSON.parse(xhr.responseText);
            if (response.success) {
                // Code is valid, redirect to dashboard or perform other actions
                window.location.href = '/admin_dashboard';
            } else {
                // Code is invalid or expired, display error message to the user
                showAlert('danger', 'Invalid or expired verification code.');
            }
        } else {
            // Handle error response
            showAlert('danger', 'Error occurred while verifying the code.');
        }
    };

    // Send the code to the server
    xhr.send(JSON.stringify({ code: verificationCode }));
}

// Add event listener to the form for form submission
document.getElementById('verificationCodeForm').addEventListener('submit', submitVerificationCode);
