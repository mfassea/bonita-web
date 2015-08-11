'use strict';

describe('Step Autogenerated Form test', function () {

    var urlParser, $window;

    beforeEach(module('autogeneratedForm'));

    var mockedWindow;
    beforeEach(function () {
        mockedWindow = {
                location: {}
        };
        module(function ($provide) {
            $provide.value('$window', mockedWindow);
        });

        inject(function ($injector) {
            urlParser = $injector.get('urlParser');
        });
    });

    it('should declare a getQueryStringParamValue function', function () {

        expect(urlParser.getQueryStringParamValue).toBeTruthy();
    });

    it('should return the query string id', function () {
        mockedWindow.location = {'search': '?id=6331560284947127611&locale=fr&mode=app'};
        var paramValue = urlParser.getQueryStringParamValue('id');
        expect(paramValue).toEqual('6331560284947127611');

        mockedWindow.location = {'search': 'id=6331560284947127611&locale=fr&mode=app'};
        paramValue = urlParser.getQueryStringParamValue('id');
        expect(paramValue).toEqual('6331560284947127611');

        mockedWindow.location = {'search': '?id=6331560284947127611&anotherid=0000000000&mode=app'};
        paramValue = urlParser.getQueryStringParamValue('id');
        expect(paramValue).toEqual('6331560284947127611');

        mockedWindow.location = {'search': '?anotherid=0000000000&mode=app'};
        paramValue = urlParser.getQueryStringParamValue('id');
        expect(paramValue).toEqual('');

    });

});