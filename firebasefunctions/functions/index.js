'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.isVeganOrNot = functions.https.onRequest((request, response) => {
	console.log(request.body);
	let body = request.body;
	
	let nonVegans = [];
	let canBeVegan = [];
	
	let ingredients = body.data.split(",");
	
	const getCanBeVeganPromise = admin.database().ref('/can-be-vegan').once('value');
	const getNonVeganPromise = admin.database().ref('/non-vegan').once('value');
	
	let dbCanBeVegan;
	let dbNonVegan;
	
	return Promise.all([getCanBeVeganPromise, getNonVeganPromise]).then(results => {
		if(results[0].hasChildren) {
			dbCanBeVegan = results[0];
		}
		if(results[1].hasChildren) {
			dbNonVegan = results[1];
		}
		
		ingredients.forEach(function(ingredient) {
			dbCanBeVegan.forEach(function(canBe) {
				if (canBe.val() === ingredient) {
					canBeVegan.push(ingredient);
				}
			  });
			  
			dbNonVegan.forEach(function(canBe) {
				if (canBe.val() === ingredient) {
					nonVegans.push(ingredient);
				}
			});
		});
		
		let jsonReturn = '{"data":{"non-vegan":' + JSON.stringify(nonVegans) + ', "can-be-vegan":' + JSON.stringify(canBeVegan) + '}}';
		console.log(jsonReturn);
		return response.send(JSON.parse(jsonReturn));
	});

});
