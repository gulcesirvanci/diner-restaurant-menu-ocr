function onDragStart(event){
    event.
    dataTransfer
    .setData('text/plain', event.target.id);

    event.
    currentTarget
    .style
    .backgroundColor = 'yellow';
}

function onDragOver(event){
    event.preventDefault();
}

function onDrop(event){
    const id = event
    .dataTransfer
    .getData('text/plain');

    const draggableElement = document.getElementById(id);
    const dropzone = event.target;
    dropzone.appendChild(draggableElement);

    draggableElement
    .style
    .backgroundColor = '#4AAE9B';
    
    event
    .dataTransfer
    .clearData();
}
function fillTheForm(button){
//th:data-dish-id="${dish.id}" th:data-menu-id="${menuId}" th:data-category-object="${dish.dishId + 'category'}" th:data-name-object="${dish.dishID+'name'}" data-description-object="${dish.dishID + 'description'}" data-price-object="${dish.dishID + 'price'}"
    dishId = button.getAttribute("data-dish-id");
    menuId = button.getAttribute("data-menu-id");
    categoryDropzoneId = button.getAttribute("data-category-object");
    nameDropzoneId = button.getAttribute("data-name-object");
    descriptionDropzoneId = button.getAttribute("data-description-object");
    priceDropzoneId = button.getAttribute("data-price-object");
    category = document.getElementById(categoryDropzoneId).value;
    console.log(descriptionDropzoneId);
    name = "";
    nameElements = document.getElementById(nameDropzoneId).getElementsByClassName("example-draggable");
    var i;
    for (i = 0; i < nameElements.length; i++) {
         name = name.concat(nameElements[i].textContent.trim());
         name = name.concat(" ");
    }
    description = ""
    descriptionElements = document.getElementById(descriptionDropzoneId).getElementsByClassName("example-draggable");
    var i;
    for (i = 0; i < descriptionElements.length; i++) {
         description = description.concat(descriptionElements[i].textContent.trim());
         description = description.concat(" ");
    }
    price = 0;
    priceElements = document.getElementById(priceDropzoneId).getElementsByClassName("example-draggable");
    if(priceElements.length > 0)
        price = priceElements[0].textContent.trim().replace(/[^\d.,-]/g, '');//BURAYA BAK
    dishIdField = document.getElementById("id");
    dishIdField.value = dishId;
    categoryIdField = document.getElementById("categoryId");
    categoryIdField.value = category;
    nameField = document.getElementById("item-name");
    nameField.value = name;
    descriptionField = document.getElementById("item-description");
    descriptionField.value = description;
    priceField = document.getElementById("item-price");
    priceField.value = price;
    document.getElementById("updateForm").submit();
}

function removeItem(button){
    dishId = button.getAttribute("data-dish-id");
    document.getElementById("deletedDishId").value = dishId;
    document.getElementById("deleteForm").submit();
}


