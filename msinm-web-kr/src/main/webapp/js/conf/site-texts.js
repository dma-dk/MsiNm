
/**
 * Translations.
 * Danish added
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',
            'LANG_KO' : 'Korean',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_REPORT': 'Report',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register',

            'FOOTER_COPYRIGHT': '&copy; 2014 KRISO',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',

            'FRONT_PAGE_TEASER': '<h1>Welcome to MSI-NM</h1><p>MSI-NM is an effort by the Danish Maritime Authority to combine MSI and NM P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Active Warnings</h2><p>For a detailed list of the active MSI-NM warnings, please go to the <a href="/search.html">Search</a> section.</p>',
            'FRONT_PAGE_FIRING_TITLE': '<h2>Firing Exercises</h2>',
            'FRONT_PAGE_FIRING_DESC': '<p>Please find a summary of the firing exercises below</p>',
            'FRONT_PAGE_FIRING_TODAY': '<h4>Firing Exercises Today</h4>',
            'FRONT_PAGE_FIRING_TOMORROW': '<h4>Firing Exercises Tomorrow</h4>',
            'FRONT_PAGE_MISC': '<h2>Report Observations</h2><p>If you observe an incident relevant to the Korean maritime community, '
                + 'please file a report in the <a href="/report.html">Report</a> section</p>'
                + '<h2>Other sources</h2>'
                + '<ul><li><a href="http://http://kriso.re.kr/" target="_blank">Kriso</a></li></ul>'

        });

        $translateProvider.translations('ko', {
            'LANG_EN' : '영어',
            'LANG_KO' : '한국어',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': '홈',
            'MENU_SEARCH': '검색',
            'MENU_REPORT': '보고서',
            'MENU_ADMIN': '관리',
            'MENU_LOGIN': '로그인',
            'MENU_LOGOUT': '로그 아웃',
            'MENU_REGISTER': '회원 가입',

            'FOOTER_COPYRIGHT': '&copy; 2014 선박해양플랜트연구소',
            'FOOTER_DISCLAIMER': '책임의 한계',
            'FOOTER_COOKIES': '쿠키',

            'FRONT_PAGE_TEASER': '<h1>MSI-NM 에 오신 것을 환영합니다</h1><p>MSI-NM은 MSI와 NM P & T를 결합하는 덴마크어 해양 당국의 노력.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>활성 경고</h2><p>활성 MSI-NM 경고의 자세한 목록은 <a href="/search.html">검색</a> 섹션으로 이동하십시오.</p>',
            'FRONT_PAGE_FIRING_TITLE': '<h2>발사 연습</h2>',
            'FRONT_PAGE_FIRING_DESC': '<p>아래의 사격 연습의 요약을 찾아주세요</p>',
            'FRONT_PAGE_FIRING_TODAY': '<h4>오늘 연습을 발사</h4>',
            'FRONT_PAGE_FIRING_TOMORROW': '<h4>발사 연습 내일</h4>',
            'FRONT_PAGE_MISC': '<h2>보고서 관찰</h2><p>당신이 한국의 해상 지역 사회에 관련된 사건을 관찰하는 경우, <a href="/report.html">보고서</a> 절에 신고하시기 바랍니다.</p> '
                + '<h2>기타 소스</h2>'
                + '<ul><li><a href="http://http://kriso.re.kr/" target="_blank">선박해양플랜트연구소</a></li></ul>'
        });

        $translateProvider.preferredLanguage('ko');

    }]);

