/* jshint jquery: true */
/* global FingerBlast: true */

$(function() {
	'use strict';

	var doc;
	var device;
	var windowWidth;
	var windowHeight;
	var pageHeight;
	var contentPadding;
	var footerHeight;
	var navComponentLinks;
	var componentsList;
	var componentLinks;
	var contentSection;
	var currentActive;
	var topCache;
	var win;
	var bod;
	var eventListeners;
	var toolbarToggle;

	var initialize = function() {
		currentActive = 0;
		topCache = [];
		win = $(window);
		doc = $(document);
		bod = $(document.body);
		device = device || $('.js-device');
		navComponentLinks = $('.js-jump-menu');
		componentsList = $('.js-component-group');
		componentLinks = $('.component-example a');
		contentSection = $('.component');
		topCache = contentSection.map(function() {
			return $(this).offset().top;
		});
		windowHeight = $(window).height() / 3;
		windowWidth = $(window).width();
		pageHeight = $(document).height();
		contentPadding = parseInt($('.docs-content').css('padding-bottom'), 10);
		footerHeight = $('.docs-footer').outerHeight(false);
		toolbarToggle = $('.js-docs-component-toolbar');

		// Device placement
		if(windowWidth >= 768 && device.offset()) {
			device.initialLeft = device.offset().left;
			device.initialTop = device.initialTop || device.offset().top;
			device.dockingOffset = ($(window).height() - device.height()) / 2;
		}

		checkDesktopContent();
		calculateScroll();
		calculateToggle();

		if(!eventListeners) {
			addEventListeners();
		}
	};

	var addEventListeners = function() {
		eventListeners = true;

		device.on('click', function(e) {
			e.preventDefault();
		});

		// Mobile navigation
		$('.js-docs-nav-trigger').on('click', function() {
			var nav = $('.docs-nav-group');
			var trigger = $('.js-docs-nav-trigger');

			trigger.toggleClass('mui-active');
			nav.toggleClass('mui-active');
		});

		navComponentLinks.click(function(e) {
			e.stopPropagation();
			e.preventDefault();
			componentsList.toggleClass('mui-active');
		});

		doc.on('click', function() {
			componentsList.removeClass('mui-active');
		});

		win.on('scroll', calculateScroll);
		win.on('scroll', calculateToggle);
	};

	var checkDesktopContent = function() {
		windowWidth = $(window).width();
		if(windowWidth <= 768) {
			var content = $('.content');
			if(content.length > 1) {
				$(content[0]).remove();
			}
		}
	};

	var calculateScroll = function() {
		// if small screen don't worry about this
		if(windowWidth <= 768) {
			return;
		}

		// Save scrollTop value
		var contentSectionItem;
		var currentTop = win.scrollTop();

		// exit if no device
		if(!device.length) {
			return;
		}

		if((device.initialTop - currentTop) <= device.dockingOffset) {
			device[0].className = 'device device-fixed';
			device.css({
				top: device.dockingOffset
			});
		} else {
			device[0].className = 'device';
			device[0].setAttribute('style', '');
		}

		function updateContent(content) {
			$('#iwindow').html(content);
		}

		// Injection of components into device
		for(var l = contentSection.length; l--;) {
			if((topCache[l] - currentTop) < windowHeight) {
				if(currentActive === l) {
					return;
				}
				currentActive = l;
				bod.find('.component.mui-active').removeClass('mui-active');
				contentSectionItem = $(contentSection[l]);
				contentSectionItem.addClass('mui-active');
				if(contentSectionItem.attr('id')) {
					device.attr('id', contentSectionItem.attr('id') + 'InDevice');
				} else {
					device.attr('id', '');
				}
				if(!contentSectionItem.hasClass('informational')) {
					updateContent(contentSectionItem.find('.highlight .language-html').text());
				}
				break;
			}
		}

	};

	// Toolbar toggle
	var calculateToggle = function() {
		var currentTop = win.scrollTop();
		var headerHeight = $('.docs-sub-header').outerHeight();

		if(currentTop >= headerHeight) {
			toolbarToggle.addClass('visible');
		} else if(currentTop <= headerHeight) {
			toolbarToggle.removeClass('visible');
			componentsList.removeClass('mui-active');
		}
	};

	$(window).on('load resize', initialize);
	$(window).on('load', function() {
		// 	window.FingerBlast && (new FingerBlast('.device-content'));
		var isSafari = navigator.userAgent.indexOf("Safari") > -1 && navigator.userAgent.indexOf("Chrome") < 1;
		!isSafari && mui('.highlight').each(function(index, element) {
			var child = element.children[0];
			var button = document.createElement("button");
			button.className = 'clip'
			button.innerHTML = '<img src="data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9IjEwMjQiIHdpZHRoPSI4OTYiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+ICA8cGF0aCBkPSJNMTI4IDc2OGgyNTZ2NjRIMTI4di02NHogbTMyMC0zODRIMTI4djY0aDMyMHYtNjR6IG0xMjggMTkyVjQ0OEwzODQgNjQwbDE5MiAxOTJWNzA0aDMyMFY1NzZINTc2eiBtLTI4OC02NEgxMjh2NjRoMTYwdi02NHpNMTI4IDcwNGgxNjB2LTY0SDEyOHY2NHogbTU3NiA2NGg2NHYxMjhjLTEgMTgtNyAzMy0xOSA0NXMtMjcgMTgtNDUgMTlINjRjLTM1IDAtNjQtMjktNjQtNjRWMTkyYzAtMzUgMjktNjQgNjQtNjRoMTkyQzI1NiA1NyAzMTMgMCAzODQgMHMxMjggNTcgMTI4IDEyOGgxOTJjMzUgMCA2NCAyOSA2NCA2NHYzMjBoLTY0VjMyMEg2NHY1NzZoNjQwVjc2OHpNMTI4IDI1Nmg1MTJjMC0zNS0yOS02NC02NC02NGgtNjRjLTM1IDAtNjQtMjktNjQtNjRzLTI5LTY0LTY0LTY0LTY0IDI5LTY0IDY0LTI5IDY0LTY0IDY0aC02NGMtMzUgMC02NCAyOS02NCA2NHoiIC8+PC9zdmc+">'
			child.appendChild(button)
			var clip = new Clipboard('.clip', {
				text: function(trigger) {
					if(trigger.classList.contains('iconlist')) {
						return trigger.children[0].className;
					}
					return trigger.parentNode.firstChild.innerText;
				}
			});
			clip.on('success', function(e) {
				e.clearSelection();
				mui.toast('复制成功!');
			});
		})
	});
});